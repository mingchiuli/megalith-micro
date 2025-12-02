use axum::{
    BoxError, Router,
    middleware::{self},
    routing::{any, get},
};
use micro_gateway_rs::{handler::main_handler, layer};
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
use std::{env, net::SocketAddr};
use tokio::signal;
use tracing_opentelemetry::OpenTelemetryLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const LOGO: &str = r#"
    _
   / \   __  ___   _ _ __ ___
  / _ \  \ \/ / | | | '_ ` _ \
 / ___ \  >  <| |_| | | | | | |
/_/   \_\/_/\_\\__,_|_| |_| |_|
"#;

fn resource() -> Resource {
    Resource::builder()
        .with_service_name("micro-gateway-rs")
        .with_attributes([KeyValue::new("service.version", env!("CARGO_PKG_VERSION"))])
        .build()
}

async fn init_tracer_provider(http_client: &reqwest::Client) -> SdkTracerProvider {
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
        .with_sampler(Sampler::AlwaysOn)
        .with_resource(resource())
        .build()
}

async fn init_meter_provider(http_client: &reqwest::Client) -> SdkMeterProvider {
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

async fn init_logger_provider(http_client: &reqwest::Client) -> SdkLoggerProvider {
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

#[tokio::main]
async fn main() -> Result<(), BoxError> {
    // Initialize logging
    if env::var("RUST_LOG").is_err() {
        unsafe {
            env::set_var("RUST_LOG", "info");
        }
    }

    // Initialize OpenTelemetry
    let http_client = reqwest::Client::new();

    let tracer_provider = init_tracer_provider(&http_client).await;
    global::set_tracer_provider(tracer_provider.clone());
    let tracer = tracer_provider.tracer("micro-gateway-rs");

    let meter_provider = init_meter_provider(&http_client).await;
    global::set_meter_provider(meter_provider.clone());

    let logger_provider = init_logger_provider(&http_client).await;

    // Setup tracing subscriber with OpenTelemetry layers
    tracing_subscriber::registry()
        .with(tracing_subscriber::fmt::layer())
        .with(tracing_subscriber::EnvFilter::from_default_env())
        .with(OpenTelemetryLayer::new(tracer))
        .with(OpenTelemetryTracingBridge::new(&logger_provider))
        .init();

    // Run the async main function
    let result = async_main().await;

    // Shutdown OpenTelemetry providers
    let _ = tracer_provider.shutdown();
    let _ = meter_provider.shutdown();
    let _ = logger_provider.shutdown();

    println!("Server shutdown completed");
    result
}

async fn async_main() -> Result<(), BoxError> {
    tracing::info!("{}", LOGO);

    // build our application with a single route
    let app = Router::new()
        .route("/actuator/health", get(|| async { "OK" }))
        .route("/{*wildcard}", any(main_handler::handle))
        .layer(middleware::from_fn(layer::auth::process));

    // run our app with hyper, listening globally on port 8008
    let port = env::var("PORT").unwrap_or_else(|_| "8088".to_string());
    let addr: SocketAddr = format!("0.0.0.0:{}", port).parse()?;
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;

    Ok(())
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
