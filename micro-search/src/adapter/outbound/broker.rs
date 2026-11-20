use crate::application::rebuild::Conflict;
use anyhow::{Context, Result, ensure};
use lapin::{
    Channel, ExchangeKind,
    options::*,
    types::{AMQPValue, FieldTable},
};

pub(crate) const QUEUES: [(&str, &str); 2] = [
    ("blog.change.queue.es", "blog.change.fanout.exchange"),
    (
        "blog.admin.change.queue.es",
        "blog.admin.change.fanout.exchange",
    ),
];
fn table(args: Vec<(&str, AMQPValue)>) -> FieldTable {
    let mut t = FieldTable::default();
    for (k, v) in args {
        t.insert(k.into(), v);
    }
    t
}
pub async fn topology(ch: &Channel) -> Result<()> {
    let queue = QueueDeclareOptions {
        durable: true,
        ..Default::default()
    };
    let exchange = ExchangeDeclareOptions {
        durable: true,
        ..Default::default()
    };
    for (q, fanout) in QUEUES {
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
#[derive(Clone, Default)]
pub struct Broker(std::sync::Arc<tokio::sync::RwLock<Option<Channel>>>);

impl Broker {
    pub async fn set_channel(&self, channel: Option<Channel>) {
        *self.0.write().await = channel;
    }

    pub async fn ready(&self) -> bool {
        self.0
            .read()
            .await
            .as_ref()
            .is_some_and(|channel| channel.status().connected())
    }
}

pub async fn quiescent(broker: &Broker) -> Result<()> {
    let ch = broker
        .0
        .read()
        .await
        .clone()
        .context("RabbitMQ unavailable")?;
    for (q, _) in QUEUES {
        for suffix in ["", ".retry.1", ".retry.2", ".retry.3", ".dlq"] {
            let name = format!("{q}{suffix}");
            let res = ch
                .queue_declare(
                    name.clone().into(),
                    QueueDeclareOptions {
                        passive: true,
                        ..Default::default()
                    },
                    FieldTable::default(),
                )
                .await?;
            ensure!(
                res.message_count() == 0 && res.consumer_count() == 0,
                Conflict(format!("search queue must be empty and stopped: {name}"))
            );
        }
    }
    Ok(())
}
