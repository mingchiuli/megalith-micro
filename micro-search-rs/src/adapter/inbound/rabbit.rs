use crate::{
    adapter::outbound::broker::{QUEUES, topology},
    application::Search,
};
use anyhow::{Context, Result, bail};
use futures_util::StreamExt;
use lapin::{
    Channel,
    options::*,
    types::{AMQPValue, FieldTable},
};
use tracing::{Instrument, error, warn};

use megalith_messaging::republish;

pub async fn supervise(app: Search) {
    loop {
        if let Err(error) = session(&app).await {
            error!(%error, "RabbitMQ session ended; reconnecting");
        }
        app.broker.set_channel(None).await;
        tokio::time::sleep(std::time::Duration::from_secs(2)).await;
    }
}

async fn session(app: &Search) -> Result<()> {
    let connection = lapin::Connection::connect(app.cfg.amqp.as_str(), Default::default()).await?;
    let channel = connection.create_channel().await?;
    topology(&channel).await?;
    let mut workers = tokio::task::JoinSet::new();
    if !app.cfg.maintenance {
        for (admin, concurrency) in [(false, 10), (true, 4)] {
            for _ in 0..concurrency {
                workers.spawn(consume(
                    app.clone(),
                    admin,
                    connection.create_channel().await?,
                ));
            }
        }
    }
    app.broker.set_channel(Some(channel.clone())).await;
    while channel.status().connected() {
        tokio::select! {
            worker = workers.join_next(), if !workers.is_empty() => {
                return worker.context("consumer stopped")?.context("RabbitMQ consumer task failed")?;
            }
            _ = tokio::time::sleep(std::time::Duration::from_secs(1)) => {}
        }
    }
    bail!("RabbitMQ channel disconnected")
}

async fn consume(app: Search, admin: bool, ch: Channel) -> Result<()> {
    let q = QUEUES[admin as usize].0;
    ch.confirm_select(Default::default()).await?;
    ch.basic_qos(1, Default::default()).await?;
    let mut consumer = ch
        .basic_consume(
            q.into(),
            "".into(),
            Default::default(),
            FieldTable::default(),
        )
        .await?;
    while let Some(delivery) = consumer.next().await {
        let d = delivery?;
        let span = tracing::info_span!("consume blog event", messaging.destination.name = q);
        if let Some(t) = d.properties.headers() {
            let mut headers = reqwest::header::HeaderMap::new();
            for key in ["traceparent", "tracestate", "baggage"] {
                if let Some(AMQPValue::LongString(s)) = t.inner().get(key)
                    && let Ok(v) = reqwest::header::HeaderValue::from_bytes(s.as_bytes())
                {
                    headers.insert(key, v);
                }
            }
            megalith_otel::set_parent_from_headers(&span, &headers);
        }
        async {
            let result = async {
                if admin {
                    app.admin_event(serde_json::from_slice(&d.data)?).await
                } else {
                    app.public_event(serde_json::from_slice(&d.data)?).await
                }
            }
            .await;
            if let Err(e) = result {
                warn!(error=%e,queue=q,"blog projection failed");
                if let Err(e) = republish(&ch, q, &d).await {
                    error!(error=%e,"retry publication failed");
                    d.nack(BasicNackOptions {
                        requeue: true,
                        ..Default::default()
                    })
                    .await?;
                    return Ok::<(), anyhow::Error>(());
                }
            }
            d.ack(BasicAckOptions::default()).await?;
            Ok(())
        }
        .instrument(span)
        .await?;
    }
    bail!("RabbitMQ consumer cancelled: {q}")
}
