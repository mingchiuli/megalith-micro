use micro_sync_rs::extractor::header_extractor::WarpHeaderExtractor;
use micro_sync_rs::handler::broadcast::ws_handler;
use micro_sync_rs::room::room::RoomManager;
use opentelemetry::metrics::Counter;
use opentelemetry::{KeyValue, global, trace::TracerProvider};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;
use opentelemetry_otlp::{
    LogExporter, MetricExporter, SpanExporter, WithExportConfig, WithHttpConfig,
};
use opentelemetry_sdk::{
    Resource,
    logs::SdkLoggerProvider,
    metrics::{PeriodicReader, SdkMeterProvider},
    trace::{Sampler, SdkTracerProvider},
};
use serde_json::json;
use std::env;
use std::sync::Arc;
use tokio::signal;
use tokio::sync::Mutex;
use tracing::Instrument;
use tracing_opentelemetry::{OpenTelemetryLayer, OpenTelemetrySpanExt};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};
use warp::Filter;
use warp::filters::header::headers_cloned;
use warp::http::HeaderMap;
use warp::reject::Rejection;
use warp::reply::json;

const LOGO: &str = r#"
 _    _  ___  ____  ____
| |  | |/ _ \|  _ \|  _ \
| |/\| | |_| | |_) | |_) |
\  /\  /  _  |  _ <|  __/
 \/  \/_/ \_\_| \_\_|
"#;

fn resource() -> Resource {
    Resource::builder()
        .with_service_name("micro-sync-rs")
        .with_attributes([KeyValue::new("service.version", env!("CARGO_PKG_VERSION"))])
        .build()
}

fn init_tracer_provider(http_client: &reqwest::blocking::Client) -> SdkTracerProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/traces".to_string());

    let exporter = SpanExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create span exporter");

    SdkTracerProvider::builder()
        .with_batch_exporter(exporter)
        .with_sampler(Sampler::TraceIdRatioBased(0.5))
        .with_resource(resource())
        .build()
}

fn init_meter_provider(http_client: &reqwest::blocking::Client) -> SdkMeterProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_METRICS_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/metrics".to_string());

    let exporter = MetricExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create metric exporter");

    let reader = PeriodicReader::builder(exporter).build();

    SdkMeterProvider::builder()
        .with_reader(reader)
        .with_resource(resource())
        .build()
}

fn init_logger_provider(http_client: &reqwest::blocking::Client) -> SdkLoggerProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_LOGS_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/logs".to_string());

    let exporter = LogExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create log exporter");

    SdkLoggerProvider::builder()
        .with_batch_exporter(exporter)
        .with_resource(resource())
        .build()
}

fn main() {
    // Initialize logging
    if env::var("RUST_LOG").is_err() {
        unsafe {
            env::set_var("RUST_LOG", "info");
        }
    }

    let http_client = reqwest::blocking::Client::new();

    // Initialize OpenTelemetry BEFORE entering async runtime (blocking clients)
    let tracer_provider = init_tracer_provider(&http_client);
    global::set_tracer_provider(tracer_provider.clone());
    let tracer = tracer_provider.tracer("micro-sync-rs");

    let meter_provider = init_meter_provider(&http_client);
    global::set_meter_provider(meter_provider.clone());

    global::set_text_map_propagator(opentelemetry_sdk::propagation::TraceContextPropagator::new());

    let logger_provider = init_logger_provider(&http_client);

    // Setup tracing subscriber with OpenTelemetry layers
    tracing_subscriber::registry()
        .with(tracing_subscriber::fmt::layer())
        .with(tracing_subscriber::EnvFilter::from_default_env())
        .with(OpenTelemetryLayer::new(tracer))
        .with(OpenTelemetryTracingBridge::new(&logger_provider))
        .init();

    // Build the Tokio runtime
    let runtime = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .expect("Failed to create Tokio runtime");

    // Run the async main function
    runtime.block_on(async_main());

    // Shutdown OpenTelemetry providers AFTER runtime is done (outside async context)
    let _ = tracer_provider.shutdown();
    let _ = meter_provider.shutdown();
    let _ = logger_provider.shutdown();

    println!("Server shutdown completed");
}

async fn async_main() {
    tracing::info!("{}", LOGO);

    let room_manager = Arc::new(Mutex::new(RoomManager::new()));
    let room_manager_ws = room_manager.clone();
    let room_manager_rooms = room_manager.clone();

    // WebSocket 路由 - 提取 headers 用于链路追踪
    let ws_routes = warp::path("rooms")
        .and(warp::path::param::<String>())
        .and(warp::header::headers_cloned())
        .and(warp::ws())
        .and(warp::any().map(move || room_manager_ws.clone()))
        .and_then(ws_handler);

    // 简单的健康检查路由
    let health_check = warp::path("actuator")
        .and(warp::path("health"))
        .and(warp::get())
        .map(|| "OK");

    // Create metrics
    let meter = global::meter("micro-sync-rs");
    let request_counter: Counter<u64> = meter
        .u64_counter("http_requests_total")
        .with_description("Total number of HTTP requests")
        .build();

    let check_rooms = warp::path("rooms")
        .and(warp::path("exist"))
        .and(warp::path::param::<String>())
        .and(warp::get())
        .and(headers_cloned())
        .and_then(move |room_id: String, headers: HeaderMap| {
            // 新增：接收 headers 参数
            let room_manager = room_manager_rooms.clone();
            let counter = request_counter.clone();
            async move { check_room_exists(room_id, room_manager, counter, headers).await }
        });

    // 合并所有路由
    let routes = ws_routes.or(health_check).or(check_rooms);

    // 创建服务器任务
    let (_, server) =
        warp::serve(routes).bind_with_graceful_shutdown(([0, 0, 0, 0], 8089), shutdown_signal());

    // 等待服务器运行完成
    server.await;
}

async fn check_room_exists(
    room_id: String,
    room_manager: Arc<Mutex<RoomManager>>,
    counter: Counter<u64>,
    headers: HeaderMap,
) -> Result<warp::reply::Json, Rejection> {
    // 1. 提取上游 Trace Context
    tracing::info!("headerMap:{:?}", headers);
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&WarpHeaderExtractor(&headers))
    });

    // 2. 创建 Span 并关联父上下文
    let span = tracing::info_span!(
        "websocket_connection",
        room_id = %room_id,
        otel.name = format!("WebSocket: /rooms/{}", room_id)
    );
    let _ = span.set_parent(parent_context);

    // 3. 核心修复：用 instrument 包裹所有异步逻辑（替代 span.entered()）
    let result = async move {
        // 业务逻辑：跨 await 完全安全（instrument 保证 Span 上下文自动传播）
        counter.add(1, &[KeyValue::new("endpoint", "check_room_exists")]);
        let room_manager = room_manager.lock().await; // 无编译错误
        let exists = room_manager.room_exists(&room_id);
        tracing::info!(room_id = %room_id, exists = %exists, "Checking room existence");

        Ok(json(&json!({
            "code": 200,
            "msg": "success",
            "data": {
                "exists": exists
            }
        })))
    }
    .instrument(span)
    .await; // instrument 满足 Send，跨 await 安全

    result
}

async fn shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("Failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("Failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();
    tokio::select! {
        _ = ctrl_c => {
            tracing::info!("Ctrl-C received");
        },
        _ = terminate => {
            tracing::info!("Terminate signal received");
        },
    }
    tracing::info!("Shutdown signal received");
}
