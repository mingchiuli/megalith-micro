use micro_sync_rs::otel::otel::{init_logger_provider, init_meter_provider, init_tracer_provider};
use micro_sync_rs::room::broadcast::ws_handler;
use micro_sync_rs::room::room::RoomManager;
use micro_sync_rs::room::room_checker::check_room_exists;
use micro_sync_rs::shutdown::shutdown::shutdown_signal;
use opentelemetry::metrics::Counter;
use opentelemetry::{global, trace::TracerProvider};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;

use std::env;
use std::sync::Arc;
use tokio::sync::Mutex;
use tracing_opentelemetry::OpenTelemetryLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};
use warp::Filter;
use warp::filters::header::headers_cloned;
use warp::http::HeaderMap;

const LOGO: &str = r#"
 _    _  ___  ____  ____
| |  | |/ _ \|  _ \|  _ \
| |/\| | |_| | |_) | |_) |
\  /\  /  _  |  _ <|  __/
 \/  \/_/ \_\_| \_\_|
"#;

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
