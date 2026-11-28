use anyhow::{Context, Result, ensure};
use cache_rs::{Cache, CacheConfig, Contract, EvictionConfig, RabbitConfig, TransportMode};
use config::{Config, File, FileFormat};
use redis::aio::ConnectionManager;
use std::{env, sync::Arc, time::Duration};
use url::Url;

pub struct Settings(Config);

impl Settings {
    pub fn load(yaml: &str) -> Result<Self> {
        Ok(Self(
            Config::builder()
                .add_source(File::from_str(yaml, FileFormat::Yaml))
                .build()?,
        ))
    }
    pub fn optional(&self, k: &str) -> Option<String> {
        env::var(k)
            .or_else(|_| env::var(k.replace(['.', '-'], "_").to_uppercase()))
            .ok()
            .or_else(|| self.0.get_string(k).ok())
    }
    pub fn get(&self, k: &str) -> Result<String> {
        self.optional(k)
            .with_context(|| format!("missing setting {k}"))
    }
    pub fn parse<T: std::str::FromStr>(&self, k: &str) -> Result<T> {
        self.get(k)?
            .parse()
            .map_err(|_| anyhow::anyhow!("invalid setting {k}"))
    }
    pub fn duration(&self, k: &str, default: Duration) -> Result<Duration> {
        let delay = self.delay(k, default)?;
        ensure!(!delay.is_zero(), "zero duration for {k}");
        Ok(delay)
    }
    pub fn delay(&self, k: &str, default: Duration) -> Result<Duration> {
        let Some(value) = self.optional(k) else {
            return Ok(default);
        };
        let d = match value.parse::<u64>() {
            Ok(ms) => Duration::from_millis(ms),
            Err(_) => Duration::try_from(value.parse::<jiff::SignedDuration>()?)?,
        };
        Ok(d)
    }
    pub async fn redis(&self) -> Result<ConnectionManager> {
        let mut url = Url::parse("redis://localhost")?;
        url.set_host(Some(&self.get("spring.data.redis.host")?))?;
        url.set_port(Some(self.parse("spring.data.redis.port")?))
            .map_err(|_| anyhow::anyhow!("invalid Redis port"))?;
        if let Some(password) = self.optional("spring.data.redis.password") {
            url.set_password(Some(&password))
                .map_err(|_| anyhow::anyhow!("invalid Redis password"))?;
        }
        Ok(ConnectionManager::new(redis::Client::open(url)?).await?)
    }
    pub fn amqp(&self) -> Result<String> {
        let mut url = Url::parse("amqp://localhost")?;
        url.set_host(Some(&self.get("spring.rabbitmq.host")?))?;
        url.set_port(Some(self.parse("spring.rabbitmq.port")?))
            .map_err(|_| anyhow::anyhow!("invalid Rabbit port"))?;
        url.set_username(&self.get("spring.rabbitmq.username")?)
            .map_err(|_| anyhow::anyhow!("invalid Rabbit username"))?;
        url.set_password(Some(&self.get("spring.rabbitmq.password")?))
            .map_err(|_| anyhow::anyhow!("invalid Rabbit password"))?;
        url.path_segments_mut()
            .expect("AMQP URL")
            .clear()
            .push(&self.get("spring.rabbitmq.virtual-host")?);
        Ok(url.into())
    }
    pub async fn cache(
        &self,
        redis: ConnectionManager,
        contracts: &[Contract],
    ) -> Result<Arc<Cache>> {
        let mut c = CacheConfig::default();
        if self
            .optional("megalith.cache.local.initial-capacity")
            .is_some()
        {
            c.initial_capacity = self.parse("megalith.cache.local.initial-capacity")?;
        }
        if self
            .optional("megalith.cache.local.maximum-entries")
            .is_some()
        {
            c.maximum_entries = self.parse("megalith.cache.local.maximum-entries")?;
        }
        if self.optional("megalith.cache.local.ttl-jitter").is_some() {
            c.local_ttl_jitter = self.parse("megalith.cache.local.ttl-jitter")?;
        }
        c.wait_timeout =
            self.duration("megalith.cache.single-flight.wait-timeout", c.wait_timeout)?;
        c.lock_entry_ttl = self.duration(
            "megalith.cache.single-flight.lock-entry-ttl",
            c.lock_entry_ttl,
        )?;
        let mode = match self
            .optional("megalith.cache.eviction.transport")
            .as_deref()
            .unwrap_or("AUTO")
        {
            "AUTO" => TransportMode::Auto,
            "REDIS" => TransportMode::Redis,
            "RABBIT" => TransportMode::Rabbit,
            _ => anyhow::bail!("invalid cache transport"),
        };
        let rabbit = match (
            self.optional("megalith.cache.eviction.rabbit.exchange"),
            self.optional("megalith.cache.eviction.rabbit.queue-prefix"),
        ) {
            (Some(exchange), Some(queue_prefix)) => Some(RabbitConfig {
                url: self.amqp()?,
                exchange,
                queue_prefix,
                confirm_timeout: self.duration(
                    "megalith.cache.eviction.rabbit.confirm-timeout",
                    Duration::from_secs(5),
                )?,
            }),
            _ => None,
        };
        let redis_topic = self
            .optional("megalith.cache.eviction.redis.topic")
            .unwrap_or_else(|| "megalith.cache.evict".into());
        Ok(Arc::new(
            Cache::start(
                c,
                redis,
                EvictionConfig {
                    mode,
                    rabbit,
                    redis_topic,
                },
                contracts,
            )
            .await?,
        ))
    }
}
