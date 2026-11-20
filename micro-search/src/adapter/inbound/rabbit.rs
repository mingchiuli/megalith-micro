use crate::{
    adapter::outbound::broker::{QUEUES, topology},
    application::Search,
};
use anyhow::{Context, Result, bail, ensure};
use futures_util::StreamExt;
use lapin::{
    Channel,
    message::Delivery,
    options::*,
    types::{AMQPValue, FieldTable},
};
use tracing::{Instrument, error, warn};

const RETRY: &str = "x-megalith-retry-count";

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
async fn republish(ch: &Channel, q: &str, d: &Delivery) -> Result<()> {
    let mut headers = d.properties.headers().clone().unwrap_or_default();
    let previous = match headers.inner().get(RETRY) {
        Some(AMQPValue::LongInt(n)) => *n,
        Some(AMQPValue::LongLongInt(n)) => i32::try_from(*n).unwrap_or(i32::MAX),
        Some(AMQPValue::ShortInt(n)) => i32::from(*n),
        _ => 0,
    };
    let attempt = previous.saturating_add(1);
    headers.insert(RETRY.into(), AMQPValue::LongInt(attempt));
    let (exchange, route) = if attempt <= 3 {
        (format!("{q}.retry.exchange"), format!("retry.{attempt}"))
    } else {
        (String::new(), format!("{q}.dlq"))
    };
    let mut properties = d.properties.clone().with_headers(headers);
    if properties.message_id().is_none() {
        properties = properties.with_message_id(d.delivery_tag.to_string().into());
    }
    let confirm = tokio::time::timeout(std::time::Duration::from_secs(5), async {
        ch.basic_publish(
            exchange.into(),
            route.into(),
            BasicPublishOptions {
                mandatory: true,
                ..Default::default()
            },
            &d.data,
            properties,
        )
        .await?
        .await
    })
    .await
    .context("retry confirm timed out")??;
    ensure!(
        confirm.is_ack() && confirm.take_message().is_none(),
        "retry publication was not routed and confirmed"
    );
    Ok(())
}
