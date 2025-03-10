use axum::{
    BoxError, Router,
    middleware::{self},
    routing::{any, get},
};
use micro_gateway_rs::{http::handler, layer};
use std::{env, net::SocketAddr};
use tokio::signal;

const LOGO: &str = r#"
    _
   / \   __  ___   _ _ __ ___
  / _ \  \ \/ / | | | '_ ` _ \
 / ___ \  >  <| |_| | | | | | |
/_/   \_\/_/\_\\__,_|_| |_| |_|
"#;

#[tokio::main]
async fn main() -> Result<(), BoxError> {
    // Initialize logging
    if env::var("RUST_LOG").is_err() {
        unsafe {
            env::set_var("RUST_LOG", "info");
        }
    }
    env_logger::init();

    log::info!("{}", LOGO);

    // build our application with a single route
    let app = Router::new()
        .route("/actuator/health", get(|| async { "OK" }))
        .route("/{*wildcard}", any(handler::unified_handler))
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
            log::info!("Ctrl-C received");
        },
        _ = terminate => {
            log::info!("Terminate signal received");
        },
    }
    log::info!("Shutdown signal received");
}
