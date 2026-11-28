use anyhow::{Context, Result, ensure};
use config::{Config, File, FileFormat};
use std::{env, net::SocketAddr, time::Duration};
use url::Url;

#[derive(Clone)]
pub struct Settings {
    pub bind: SocketAddr,
    pub redis: Url,
    pub amqp: Url,
    pub blog: String,
    pub engine: String,
    pub key: String,
    indexes: [String; 3],
    pub maintenance: bool,
    pub environment: String,
    pub batch: usize,
    pub page_size: usize,
    pub max_hits: usize,
    pub task_timeout: Duration,
    pub snapshot_ttl: Duration,
}

impl Settings {
    pub(crate) fn index_uid(&self, kind: crate::domain::IndexKind) -> &str {
        &self.indexes[kind as usize]
    }

    pub fn load() -> Result<Self> {
        let file = Config::builder()
            .add_source(File::from_str(
                include_str!("../application.yml"),
                FileFormat::Yaml,
            ))
            .build()?;
        let read = |key: &str| -> Result<String> {
            let upper = key.replace(['.', '-'], "_").to_uppercase();
            env::var(key)
                .or_else(|_| env::var(upper))
                .or_else(|_| file.get_string(key))
                .with_context(|| format!("invalid setting {key}"))
        };
        let mut redis = Url::parse("redis://localhost")?;
        redis.set_host(Some(&read("spring.data.redis.host")?))?;
        redis
            .set_port(Some(read("spring.data.redis.port")?.parse()?))
            .map_err(|_| anyhow::anyhow!("invalid Redis port"))?;
        if let Ok(password) = read("spring.data.redis.password")
            && !password.is_empty()
        {
            redis
                .set_password(Some(&password))
                .map_err(|_| anyhow::anyhow!("invalid Redis password"))?;
        }
        let mut amqp = Url::parse("amqp://localhost")?;
        amqp.set_host(Some(&read("spring.rabbitmq.host")?))?;
        amqp.set_port(Some(read("spring.rabbitmq.port")?.parse()?))
            .map_err(|_| anyhow::anyhow!("invalid RabbitMQ port"))?;
        amqp.set_username(&read("spring.rabbitmq.username")?)
            .map_err(|_| anyhow::anyhow!("invalid RabbitMQ username"))?;
        amqp.set_password(Some(&read("spring.rabbitmq.password")?))
            .map_err(|_| anyhow::anyhow!("invalid RabbitMQ password"))?;
        amqp.path_segments_mut()
            .expect("AMQP URL")
            .clear()
            .push(&read("spring.rabbitmq.virtual-host")?);
        let indexes = [
            read("megalith.search.index.alias")?,
            read("megalith.search.index.admin-alias")?,
            read("megalith.search.index.revision-alias")?,
        ];
        ensure!(
            indexes.iter().all(|s| !s.is_empty()
                && s.bytes()
                    .all(|c| c.is_ascii_alphanumeric() || c == b'-' || c == b'_'))
                && indexes[0] != indexes[1]
                && indexes[0] != indexes[2]
                && indexes[1] != indexes[2],
            "invalid index UIDs"
        );
        let batch = read("megalith.search.index.rebuild-batch-size")?.parse()?;
        ensure!((1..=1000).contains(&batch), "invalid rebuild batch size");
        let page_size = read("megalith.blog.blog-page-size")?.parse()?;
        ensure!(page_size > 0, "invalid page size");
        let key = read("megalith.search.meilisearch.key")?;
        Ok(Self {
            bind: SocketAddr::new(
                read("server.address")?.parse()?,
                read("server.port")?.parse()?,
            ),
            redis,
            amqp,
            indexes,
            batch,
            page_size,
            blog: read("megalith.blog.blog-url")?.trim_end_matches('/').into(),
            engine: read("megalith.search.meilisearch.url")?
                .trim_end_matches('/')
                .into(),
            key: if key.is_empty() {
                env::var("MEILI_MASTER_KEY").unwrap_or_default()
            } else {
                key
            },
            maintenance: read("megalith.search.maintenance.enabled")?.parse()?,
            environment: read("megalith.task-lock.environment")
                .or_else(|_| read("spring.profiles.active"))
                .unwrap_or_else(|_| "default".into()),
            max_hits: read("megalith.search.index.max-total-hits")?.parse()?,
            task_timeout: duration(&read("megalith.search.meilisearch.task-timeout")?)?,
            snapshot_ttl: duration(&read("megalith.search.recommendation.snapshot-ttl")?)?,
        })
    }
}
fn duration(value: &str) -> Result<Duration> {
    let duration = match value.parse::<u64>() {
        Ok(ms) => Duration::from_millis(ms),
        Err(_) => Duration::try_from(value.parse::<jiff::SignedDuration>()?)?,
    };
    ensure!(
        duration.as_millis() > 0,
        "expected a duration of at least one millisecond"
    );
    Ok(duration)
}

pub use megalith_otel::otlp_config;
