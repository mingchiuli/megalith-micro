use axum::{
    BoxError, Router,
    middleware::{self},
    routing::{any, get},
};
use micro_gateway_rs::{
    auth_process,
    config::config::{self, ConfigKey, init_config},
    handle_main, init_logger_provider, init_meter_provider, init_tracer_provider, shutdown_signal,
};
use opentelemetry::{global, trace::TracerProvider};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;

use std::{env, net::SocketAddr};
use tracing_opentelemetry::OpenTelemetryLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const LOGO: &str = r#"
    _
   / \   __  ___   _ _ __ ___
  / _ \  \ \/ / | | | '_ ` _ \
 / ___ \  >  <| |_| | | | | | |
/_/   \_\/_/\_\\__,_|_| |_| |_|
"#;

fn main() -> Result<(), BoxError> {
    init_config().unwrap_or_else(|err| {
        eprintln!("Failed to load config: {:?}", err);
        std::process::exit(1);
    });
    // Initialize logging
    unsafe {
        env::set_var("RUST_LOG", config::get_config(ConfigKey::RustLog));
    }

    // Initialize OpenTelemetry BEFORE entering async runtime
    // MUST use blocking client - BatchProcessor runs in non-tokio threads
    let http_client = reqwest::blocking::Client::new();

    let tracer_provider = init_tracer_provider(&http_client);
    global::set_tracer_provider(tracer_provider.clone());
    let tracer = tracer_provider.tracer(config::get_config(ConfigKey::ServerName));

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
    let result = runtime.block_on(async_main());

    // Shutdown OpenTelemetry providers AFTER runtime is done (outside async context)
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
        .route("/{*wildcard}", any(handle_main))
        .layer(middleware::from_fn(auth_process));

    // run our app with hyper, listening globally on port 8008
    let port = config::get_config(ConfigKey::ServerPort);
    let addr: SocketAddr = format!("0.0.0.0:{}", port).parse()?;
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;

    Ok(())
}
