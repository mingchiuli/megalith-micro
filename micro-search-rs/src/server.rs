use crate::{
    adapter::{
        inbound::{http, rabbit},
        outbound::{blog::BlogSource, meili::Meili, redis::Store},
    },
    application::Search,
    config::Settings,
};
use anyhow::Result;
use std::{sync::Arc, time::Duration};
use tokio::net::TcpListener;

pub async fn serve(cfg: Settings) -> Result<()> {
    let cfg = Arc::new(cfg);
    let store = Store::connect(cfg.redis.as_str()).await?;
    let client = reqwest::Client::builder()
        .connect_timeout(Duration::from_secs(5))
        .timeout(Duration::from_secs(60))
        .build()?;
    let app = Search {
        cfg: cfg.clone(),
        engine: Meili::new(client.clone(), &cfg),
        source: BlogSource {
            client,
            base: cfg.blog.clone(),
        },
        broker: crate::adapter::outbound::broker::Broker::default(),
        store,
    };
    app.initialize().await?;
    let listener = TcpListener::bind(cfg.bind).await?;
    let messaging = tokio::spawn(rabbit::supervise(app.clone()));
    tracing::info!(address = %listener.local_addr()?, maintenance = cfg.maintenance, "search server listening");
    let result = axum::serve(listener, http::router(app))
        .with_graceful_shutdown(shutdown_signal())
        .await;
    messaging.abort();
    let _ = messaging.await;
    Ok(result?)
}

async fn shutdown_signal() {
    let mut term = tokio::signal::unix::signal(tokio::signal::unix::SignalKind::terminate())
        .expect("install SIGTERM handler");
    tokio::select! {
        _ = tokio::signal::ctrl_c() => {},
        _ = term.recv() => {},
    }
}
