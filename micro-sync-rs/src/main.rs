use micro_sync_rs::{
    config::{self, ConfigKey, init_config},
    set_route, shutdown_signal, trace_context_middleware,
};
use std::net::SocketAddr;
use tokio::net::TcpListener;

const LOGO: &str = r#"
    _
   / \   __  ___   _ _ __ ___
  / _ \  \ \/ / | | | '_ ` _ \
 / ___ \  >  <| |_| | | | | | |
/_/   \_\/_/\_\\__,_|_| |_| |_|
"#;

fn main() {
    init_config().unwrap_or_else(|err| {
        eprintln!("Failed to load config: {:?}", err);
        std::process::exit(1);
    });
    let telemetry = config::otlp_config()
        .init(
            &config::get_config(ConfigKey::ServerName),
            env!("CARGO_PKG_VERSION"),
            &config::get_config(ConfigKey::RustLog),
            0.05,
        )
        .expect("initialize OpenTelemetry");

    // Build the Tokio runtime
    let runtime = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .expect("Failed to create Tokio runtime");

    // Run the async main function
    runtime.block_on(async {
        tracing::info!("{}", LOGO);

        let port: u16 = config::get_config(ConfigKey::ServerPort)
            .parse()
            .expect("Invalid server port configuration");
        let addr = SocketAddr::from(([0, 0, 0, 0], port));

        let listener = TcpListener::bind(addr)
            .await
            .expect("Failed to bind to address");
        tracing::info!("Server listening on {}", addr);

        axum::serve(
            listener,
            set_route()
                .await
                .expect("Failed to initialize Redis session store")
                .layer(axum::middleware::from_fn_with_state(
                    (),
                    trace_context_middleware,
                )),
        )
        .with_graceful_shutdown(shutdown_signal())
        .await
        .expect("Server error");
    });

    telemetry.shutdown();

    println!("Server shutdown completed");
}
