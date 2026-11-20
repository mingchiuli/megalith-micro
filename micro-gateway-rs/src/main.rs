use axum::{
    BoxError, Router,
    middleware::{self},
    routing::{any, get},
};
use micro_gateway_rs::{
    GatewayState, auth_process,
    config::{self, ConfigKey, init_config},
    handle_main, shutdown_signal, trace_context_middleware,
};

use std::net::SocketAddr;

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
    let telemetry = config::otlp_config().init(
        &config::get_config(ConfigKey::ServerName),
        env!("CARGO_PKG_VERSION"),
        &config::get_config(ConfigKey::RustLog),
        0.05,
    )?;

    // Build the Tokio runtime
    let runtime = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .expect("Failed to create Tokio runtime");

    // Run the async main function
    let result = runtime.block_on(async_main());

    telemetry.shutdown();

    println!("Server shutdown completed");
    result
}

async fn async_main() -> Result<(), BoxError> {
    tracing::info!("{}", LOGO);

    // build our application with a single route
    let state = GatewayState::from_config();
    let app = Router::new()
        .route("/actuator/health", get(|| async { "OK" }))
        .route("/{*wildcard}", any(handle_main))
        .layer(middleware::from_fn_with_state(state.clone(), auth_process))
        .layer(middleware::from_fn_with_state((), trace_context_middleware))
        .with_state(state);

    // run our app with hyper, listening globally on port 8008
    let port = config::get_config(ConfigKey::ServerPort);
    let addr: SocketAddr = format!("0.0.0.0:{}", port).parse()?;
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;

    Ok(())
}
