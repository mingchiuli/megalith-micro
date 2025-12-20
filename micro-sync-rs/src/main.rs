use micro_sync_rs::{
    config::config::{self, ConfigKey},
    init_logger_provider, init_meter_provider, init_tracer_provider, set_route, shutdown_signal,
};
use opentelemetry::{global, trace::TracerProvider};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;

use std::env;

use tracing_opentelemetry::OpenTelemetryLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const LOGO: &str = r#"
 _    _  ___  ____  ____
| |  | |/ _ \|  _ \|  _ \
| |/\| | |_| | |_) | |_) |
\  /\  /  _  |  _ <|  __/
 \/  \/_/ \_\_| \_\_|
"#;

fn main() {
    // Initialize logging
    unsafe {
        env::set_var("RUST_LOG", config::get_config(ConfigKey::RustLog));
    }

    let http_client = reqwest::blocking::Client::new();

    // Initialize OpenTelemetry BEFORE entering async runtime (blocking clients)
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
    runtime.block_on((async || {
        tracing::info!("{}", LOGO);

        // 创建服务器任务
        let (_, server) = warp::serve(set_route()).bind_with_graceful_shutdown(
            (
                [0, 0, 0, 0],
                config::get_config(ConfigKey::ServerPort).parse().unwrap(),
            ),
            shutdown_signal(),
        );

        // 等待服务器运行完成
        server.await;
    })());

    // Shutdown OpenTelemetry providers AFTER runtime is done (outside async context)
    let _ = tracer_provider.shutdown();
    let _ = meter_provider.shutdown();
    let _ = logger_provider.shutdown();

    println!("Server shutdown completed");
}
