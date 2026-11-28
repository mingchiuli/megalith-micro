use anyhow::{Context, Result, ensure};
use futures_util::StreamExt;
use lapin::{
    Channel, ExchangeKind,
    message::Delivery,
    options::*,
    types::{AMQPValue, FieldTable},
};
use std::future::Future;
use tracing::Instrument;
const RETRY: &str = "x-megalith-retry-count";
fn table(args: Vec<(&str, AMQPValue)>) -> FieldTable {
    let mut t = FieldTable::default();
    for (k, v) in args {
        t.insert(k.into(), v);
    }
    t
}
pub async fn declare_topology(ch: &Channel, q: &str, fanout: &str) -> Result<()> {
    let queue = QueueDeclareOptions {
        durable: true,
        ..Default::default()
    };
    let exchange = ExchangeDeclareOptions {
        durable: true,
        ..Default::default()
    };
    {
        ch.exchange_declare(
            fanout.into(),
            ExchangeKind::Fanout,
            exchange,
            FieldTable::default(),
        )
        .await?;
        ch.queue_declare(q.into(), queue, FieldTable::default())
            .await?;
        ch.queue_bind(
            q.into(),
            fanout.into(),
            "".into(),
            Default::default(),
            FieldTable::default(),
        )
        .await?;
        let retry = format!("{q}.retry.exchange");
        ch.exchange_declare(
            retry.clone().into(),
            ExchangeKind::Direct,
            exchange,
            FieldTable::default(),
        )
        .await?;
        for (i, ttl) in [5000, 30000, 300000].into_iter().enumerate() {
            let route = format!("retry.{}", i + 1);
            let name = format!("{q}.{route}");
            let args = table(vec![
                ("x-message-ttl", AMQPValue::LongInt(ttl)),
                ("x-dead-letter-exchange", AMQPValue::LongString("".into())),
                ("x-dead-letter-routing-key", AMQPValue::LongString(q.into())),
            ]);
            ch.queue_declare(name.clone().into(), queue, args).await?;
            ch.queue_bind(
                name.into(),
                retry.clone().into(),
                route.into(),
                Default::default(),
                FieldTable::default(),
            )
            .await?;
        }
        ch.queue_declare(
            format!("{q}.dlq").into(),
            queue,
            table(vec![(
                "x-message-ttl",
                AMQPValue::LongInt(14 * 86400 * 1000),
            )]),
        )
        .await?;
    }
    Ok(())
}
pub async fn republish(ch: &Channel, q: &str, d: &Delivery) -> Result<()> {
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

pub struct EventConsumer {
    stop: tokio_util::sync::CancellationToken,
    worker: tokio::task::JoinHandle<()>,
}

impl EventConsumer {
    pub async fn shutdown(mut self) {
        self.stop.cancel();
        if tokio::time::timeout(std::time::Duration::from_secs(60), &mut self.worker)
            .await
            .is_err()
        {
            tracing::warn!("event consumer did not finish in 60 seconds");
            self.worker.abort();
            let _ = self.worker.await;
        }
    }
}

pub fn consume<F, Fut>(
    url: String,
    queue: &'static str,
    exchange: &'static str,
    handler: F,
) -> EventConsumer
where
    F: Fn(Vec<u8>) -> Fut + Clone + Send + Sync + 'static,
    Fut: Future<Output = Result<()>> + Send + 'static,
{
    let stop = tokio_util::sync::CancellationToken::new();
    let shutdown = stop.clone();
    let worker = tokio::spawn(async move {
        while !shutdown.is_cancelled() {
            if let Err(error) = session(&url, queue, exchange, handler.clone(), &shutdown).await {
                tracing::error!(%error,queue,"business consumer disconnected");
            }
            tokio::select! {
                _ = shutdown.cancelled() => break,
                _ = tokio::time::sleep(std::time::Duration::from_secs(1)) => {}
            }
        }
    });
    EventConsumer { stop, worker }
}

async fn session<F, Fut>(
    url: &str,
    queue: &'static str,
    exchange: &str,
    handler: F,
    shutdown: &tokio_util::sync::CancellationToken,
) -> Result<()>
where
    F: Fn(Vec<u8>) -> Fut + Clone + Send + Sync + 'static,
    Fut: Future<Output = Result<()>> + Send + 'static,
{
    let conn = lapin::Connection::connect(url, Default::default()).await?;
    declare_topology(&conn.create_channel().await?, queue, exchange).await?;
    let mut workers = tokio::task::JoinSet::new();
    for _ in 0..10 {
        let channel = conn.create_channel().await?;
        let handler = handler.clone();
        let shutdown = shutdown.clone();
        workers.spawn(async move {
            channel.confirm_select(Default::default()).await?;
            channel.basic_qos(1, Default::default()).await?;
            let mut consumer = channel
                .basic_consume(
                    queue.into(),
                    "".into(),
                    Default::default(),
                    FieldTable::default(),
                )
                .await?;
            loop {
                let d = tokio::select! {
                    biased;
                    _ = shutdown.cancelled() => return Ok::<(),anyhow::Error>(()),
                    delivery = consumer.next() => delivery.context("consumer cancelled")??,
                };
                let span = tracing::info_span!("consume event", messaging.destination.name = queue);
                let mut headers = reqwest::header::HeaderMap::new();
                if let Some(table) = d.properties.headers() {
                    for name in ["traceparent", "tracestate", "baggage"] {
                        if let Some(AMQPValue::LongString(value)) = table.inner().get(name)
                            && let Ok(header) =
                                reqwest::header::HeaderValue::from_bytes(value.as_bytes())
                        {
                            headers.insert(name, header);
                        }
                    }
                }
                megalith_otel::set_parent_from_headers(&span, &headers);
                async {
                    if let Err(error) = handler(d.data.clone()).await {
                        tracing::warn!(%error,queue,"event processing failed");
                        if let Err(error) = republish(&channel, queue, &d).await {
                            d.nack(BasicNackOptions {
                                requeue: true,
                                ..Default::default()
                            })
                            .await?;
                            return Err(error);
                        }
                    }
                    d.ack(Default::default()).await?;
                    Ok::<(), anyhow::Error>(())
                }
                .instrument(span)
                .await?;
            }
        });
    }
    while let Some(worker) = workers.join_next().await {
        worker??;
    }
    Ok(())
}
