use crate::application::rebuild::Conflict;
use anyhow::{Context, Result, ensure};
use lapin::{Channel, options::*, types::FieldTable};

pub(crate) const QUEUES: [(&str, &str); 2] = [
    ("blog.change.queue.es", "blog.change.fanout.exchange"),
    (
        "blog.admin.change.queue.es",
        "blog.admin.change.fanout.exchange",
    ),
];
pub async fn topology(ch: &Channel) -> Result<()> {
    for (queue, exchange) in QUEUES {
        megalith_messaging::declare_topology(ch, queue, exchange).await?;
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
