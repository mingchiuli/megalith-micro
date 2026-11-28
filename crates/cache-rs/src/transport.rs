use anyhow::{Context, Result, ensure};
use futures_util::StreamExt;
use lapin::{
    BasicProperties, Channel, Connection, Consumer, ExchangeKind, options::*, types::FieldTable,
};
use redis::{aio::ConnectionManager, streams::StreamReadReply};
use serde::{Deserialize, Serialize};
use std::{collections::BTreeSet, sync::Arc, time::Duration};
use tokio::{
    sync::RwLock,
    task::JoinHandle,
    time::{sleep, timeout},
};
use uuid::Uuid;

use crate::local::LocalCache;

#[derive(Clone, Copy, Debug)]
pub enum TransportMode {
    Auto,
    Rabbit,
    Redis,
}

#[derive(Clone)]
pub struct RabbitConfig {
    pub url: String,
    pub exchange: String,
    pub queue_prefix: String,
    pub confirm_timeout: Duration,
}

#[derive(Clone)]
pub struct EvictionConfig {
    pub mode: TransportMode,
    pub rabbit: Option<RabbitConfig>,
    pub redis_topic: String,
}

impl Default for EvictionConfig {
    fn default() -> Self {
        Self {
            mode: TransportMode::Auto,
            rabbit: None,
            redis_topic: "megalith.cache.evict".into(),
        }
    }
}

#[derive(Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
struct Message {
    protocol_version: u32,
    keys: BTreeSet<String>,
}

fn invalidate(data: &[u8], local: &LocalCache) -> Result<()> {
    let message: Message = serde_json::from_slice(data)?;
    ensure!(
        message.protocol_version == 1,
        "unsupported cache eviction protocol"
    );
    for key in message.keys {
        local.invalidate(&key);
    }
    Ok(())
}

pub(crate) enum Broadcast {
    Redis {
        connection: ConnectionManager,
        topic: String,
        subscriber: String,
    },
    Rabbit {
        channel: Arc<RwLock<Option<Channel>>>,
        config: RabbitConfig,
    },
}

impl Broadcast {
    pub async fn start(
        config: EvictionConfig,
        redis: ConnectionManager,
        local: LocalCache,
    ) -> Result<(Self, JoinHandle<()>)> {
        match (config.mode, config.rabbit) {
            (TransportMode::Auto | TransportMode::Rabbit, Some(rabbit)) => {
                ensure!(
                    !rabbit.exchange.is_empty()
                        && !rabbit.queue_prefix.is_empty()
                        && !rabbit.confirm_timeout.is_zero(),
                    "invalid Rabbit cache settings"
                );
                let session = rabbit_session(&rabbit).await?;
                let channel = Arc::new(RwLock::new(Some(session.1.clone())));
                let task = tokio::spawn(consume_rabbit(
                    rabbit.clone(),
                    channel.clone(),
                    local,
                    session,
                ));
                Ok((
                    Self::Rabbit {
                        channel,
                        config: rabbit,
                    },
                    task,
                ))
            }
            (TransportMode::Rabbit, None) => {
                anyhow::bail!("Rabbit transport requires a connection, exchange and queue prefix")
            }
            _ => {
                let topic = config.redis_topic;
                ensure!(!topic.is_empty(), "Redis topic is empty");
                let id = Uuid::new_v4().to_string();
                topic_heartbeat(&redis, &topic, &id).await?;
                let task = tokio::spawn(consume_redis(
                    redis.clone(),
                    topic.clone(),
                    id.clone(),
                    local,
                ));
                Ok((
                    Self::Redis {
                        connection: redis,
                        topic,
                        subscriber: id,
                    },
                    task,
                ))
            }
        }
    }
    pub async fn available(&self) -> bool {
        match self {
            Self::Rabbit { channel, .. } => channel
                .read()
                .await
                .as_ref()
                .is_some_and(|c| c.status().connected()),
            Self::Redis { .. } => true,
        }
    }

    pub async fn unsubscribe(&self) -> Result<()> {
        if let Self::Redis {
            connection,
            topic,
            subscriber,
        } = self
        {
            redis::cmd("EVAL")
                .arg(include_str!("../script/topic-unsubscribe.lua"))
                .arg(2)
                .arg(topic)
                .arg(timeout_key(topic))
                .arg(subscriber)
                .query_async::<i64>(&mut connection.clone())
                .await?;
        }
        Ok(())
    }

    pub fn name(&self) -> &'static str {
        match self {
            Self::Redis { .. } => "redis",
            Self::Rabbit { .. } => "rabbit",
        }
    }
    pub async fn publish(&self, keys: &BTreeSet<String>) -> Result<()> {
        let bytes = serde_json::to_vec(&Message {
            protocol_version: 1,
            keys: keys.clone(),
        })?;
        match self {
            Self::Redis {
                connection, topic, ..
            } => {
                redis::cmd("XADD")
                    .arg(topic)
                    .arg("*")
                    .arg("m")
                    .arg(bytes)
                    .query_async::<String>(&mut connection.clone())
                    .await?;
            }
            Self::Rabbit { channel, config } => {
                let channel = channel
                    .read()
                    .await
                    .clone()
                    .context("cache broadcast channel disconnected")?;
                let confirm = timeout(config.confirm_timeout, async {
                    channel
                        .basic_publish(
                            config.exchange.as_str().into(),
                            "".into(),
                            BasicPublishOptions {
                                mandatory: true,
                                ..Default::default()
                            },
                            &bytes,
                            BasicProperties::default().with_content_type("application/json".into()),
                        )
                        .await?
                        .await
                })
                .await
                .context("cache eviction confirm timed out")??;
                ensure!(
                    confirm.is_ack() && confirm.take_message().is_none(),
                    "cache eviction not routed and confirmed"
                );
            }
        }
        Ok(())
    }
}

type Session = (Connection, Channel, Consumer);

async fn rabbit_session(config: &RabbitConfig) -> Result<Session> {
    let connection = Connection::connect(&config.url, Default::default()).await?;
    let channel = connection.create_channel().await?;
    channel.confirm_select(Default::default()).await?;
    channel
        .exchange_declare(
            config.exchange.as_str().into(),
            ExchangeKind::Fanout,
            ExchangeDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await?;
    let queue = format!("{}{}", config.queue_prefix, Uuid::new_v4());
    channel
        .queue_declare(
            queue.as_str().into(),
            QueueDeclareOptions {
                exclusive: true,
                auto_delete: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await?;
    channel
        .queue_bind(
            queue.as_str().into(),
            config.exchange.as_str().into(),
            "".into(),
            Default::default(),
            FieldTable::default(),
        )
        .await?;
    let consumer = channel
        .basic_consume(
            queue.into(),
            "".into(),
            Default::default(),
            FieldTable::default(),
        )
        .await?;
    Ok((connection, channel, consumer))
}

async fn consume_rabbit(
    config: RabbitConfig,
    channel: Arc<RwLock<Option<Channel>>>,
    local: LocalCache,
    mut session: Session,
) {
    loop {
        while let Some(Ok(delivery)) = session.2.next().await {
            let ack = match invalidate(&delivery.data, &local) {
                Ok(()) => delivery.ack(Default::default()).await,
                Err(error) => {
                    tracing::warn!(%error, "invalid cache message");
                    delivery.reject(BasicRejectOptions { requeue: false }).await
                }
            };
            if ack.is_err() {
                break;
            }
        }
        *channel.write().await = None;
        loop {
            sleep(Duration::from_secs(1)).await;
            if let Ok(next) = rabbit_session(&config).await {
                local.clear();
                *channel.write().await = Some(next.1.clone());
                session = next;
                break;
            }
        }
    }
}

fn timeout_key(topic: &str) -> String {
    if topic.contains('{') {
        format!("{topic}:timeout")
    } else {
        format!("{{{topic}}}:timeout")
    }
}
async fn topic_heartbeat(connection: &ConnectionManager, topic: &str, id: &str) -> Result<bool> {
    let timeouts = timeout_key(topic);
    Ok(redis::cmd("EVAL")
        .arg(include_str!("../script/topic-heartbeat.lua"))
        .arg(2)
        .arg(topic)
        .arg(timeouts)
        .arg(id)
        .query_async(&mut connection.clone())
        .await?)
}

async fn consume_redis(
    mut connection: ConnectionManager,
    topic: String,
    id: String,
    local: LocalCache,
) {
    let mut heartbeat = tokio::time::Instant::now();
    loop {
        let result: Result<()> = async {
            if tokio::time::Instant::now() >= heartbeat {
                if topic_heartbeat(&connection, &topic, &id).await? {
                    local.clear();
                }
                heartbeat = tokio::time::Instant::now() + Duration::from_secs(10);
            }
            // Read unacknowledged deliveries first after a connection loss.
            for cursor in ["0", ">"] {
                let reply: Option<StreamReadReply> = redis::cmd("XREADGROUP")
                    .arg("GROUP")
                    .arg(&id)
                    .arg("consumer")
                    .arg("COUNT")
                    .arg(100)
                    .arg("STREAMS")
                    .arg(&topic)
                    .arg(cursor)
                    .query_async(&mut connection)
                    .await?;
                for stream in reply.into_iter().flat_map(|r| r.keys) {
                    for entry in stream.ids {
                        let bytes: Vec<u8> = entry.get("m").context("missing eviction message")?;
                        invalidate(&bytes, &local)?;
                        redis::cmd("XACK")
                            .arg(&topic)
                            .arg(&id)
                            .arg(entry.id)
                            .query_async::<usize>(&mut connection)
                            .await?;
                    }
                }
            }
            Ok(())
        }
        .await;
        if result.is_ok() {
            sleep(Duration::from_millis(50)).await;
        } else if let Err(error) = result {
            heartbeat = tokio::time::Instant::now();
            tracing::warn!(%error, "Redis eviction listener interrupted");
            sleep(Duration::from_secs(1)).await;
        }
    }
}
