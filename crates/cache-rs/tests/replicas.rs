use anyhow::Result;
use cache_rs::{
    Argument, Cache, CacheConfig, Contract, EvictionConfig, RabbitConfig, TransportMode, cache,
};
use redis::{AsyncCommands, aio::ConnectionManager};
use std::{
    collections::BTreeSet,
    env,
    sync::{
        Arc,
        atomic::{AtomicUsize, Ordering},
    },
    time::Duration,
};
use uuid::Uuid;

const USER_ACCESS: Contract = Contract::new("auth-user-access", 1, Duration::from_secs(10));

struct CountedSource {
    cache: Arc<Cache>,
    loads: Arc<AtomicUsize>,
}

impl CountedSource {
    #[cache(USER_ACCESS)]
    async fn count(&self, topic: &str, id: i64) -> Result<Option<i32>> {
        self.loads.fetch_add(1, Ordering::SeqCst);
        tokio::time::sleep(Duration::from_millis(30)).await;
        Ok(Some(123))
    }
}

#[tokio::test]
#[ignore = "requires a dedicated Redis 8 and RabbitMQ"]
async fn read_evict_and_reload_across_two_replicas() -> Result<()> {
    let redis =
        ConnectionManager::new(redis::Client::open(env::var("CACHE_TEST_REDIS_URL")?)?).await?;
    for mode in [TransportMode::Redis, TransportMode::Rabbit] {
        for tracked in [false, true] {
            exercise(&redis, mode, tracked).await?;
        }
    }
    Ok(())
}

async fn exercise(redis: &ConnectionManager, mode: TransportMode, tracked: bool) -> Result<()> {
    let topic = format!("test-{}", Uuid::new_v4());
    let transport = EvictionConfig {
        mode,
        redis_topic: topic.clone(),
        rabbit: Some(RabbitConfig {
            url: env::var("CACHE_TEST_AMQP_URL")?,
            exchange: topic.clone(),
            queue_prefix: format!("{topic}."),
            confirm_timeout: Duration::from_secs(3),
        }),
    };
    let contract = Contract {
        track_keys: tracked,
        ..USER_ACCESS
    };
    let mut replicas = Vec::new();
    for _ in 0..2 {
        replicas.push(Arc::new(
            Cache::start(
                CacheConfig::default(),
                redis.clone(),
                transport.clone(),
                &[contract],
            )
            .await?,
        ));
    }
    let args = [Argument::string(&topic)];
    let desc = contract.descriptor;
    let key = desc.key(&args);
    // StringCodec stores Jackson JSON directly in Redis.
    redis
        .clone()
        .set_ex::<_, _, ()>(&key, br#"{"count":42}"#.as_slice(), 10)
        .await?;
    for replica in &replicas {
        let entry: Option<serde_json::Value> = replica
            .read(desc, &args, || async {
                anyhow::bail!("source called on L2 hit")
            })
            .await?;
        assert_eq!(entry, Some(serde_json::json!({"count":42})));
    }
    if tracked {
        assert!(
            replicas[0]
                .registry()
                .registered_keys(desc)
                .await?
                .contains(&key)
        );
    }
    replicas[0].evict(BTreeSet::from([key.clone()])).await?;
    assert!(!redis.clone().exists::<_, bool>(&key).await?);
    tokio::time::timeout(Duration::from_secs(4), async {
        loop {
            let entry = replicas[1]
                .read(desc, &args, || async {
                    Ok(Some(serde_json::json!({"count":43})))
                })
                .await?;
            if entry == Some(serde_json::json!({"count":43})) {
                break;
            }
            tokio::time::sleep(Duration::from_millis(10)).await;
        }
        Ok::<_, anyhow::Error>(())
    })
    .await??;
    assert_eq!(
        redis.clone().get::<_, String>(&key).await?,
        r#"{"count":43}"#
    );
    let loads = Arc::new(AtomicUsize::new(0));
    let mut tasks = tokio::task::JoinSet::new();
    for i in 0..12 {
        let source = CountedSource {
            cache: replicas[i % 2].clone(),
            loads: loads.clone(),
        };
        let topic = topic.clone();
        tasks.spawn(async move { source.count(&topic, 1).await });
    }
    while let Some(task) = tasks.join_next().await {
        assert_eq!(task??, Some(123));
    }
    assert_eq!(loads.load(Ordering::SeqCst), 1);
    let key = desc.key(&[Argument::string(&topic), Argument::long(1)]);
    assert_eq!(redis.clone().get::<_, String>(key).await?, "123");
    Ok(())
}

#[tokio::test]
#[ignore = "requires a dedicated Redis 8"]
async fn corrupt_null_failed_and_expired_loads_preserve_tracking_and_ttl() -> Result<()> {
    let redis =
        ConnectionManager::new(redis::Client::open(env::var("CACHE_TEST_REDIS_URL")?)?).await?;
    let mut contract = Contract::new("fixture-tracked", 1, Duration::from_millis(180));
    contract.track_keys = true;
    let name = Uuid::new_v4().to_string();
    let cache = Cache::start(
        CacheConfig::default(),
        redis.clone(),
        EvictionConfig {
            redis_topic: format!("cache-{name}"),
            ..Default::default()
        },
        &[contract],
    )
    .await?;
    let desc = contract.descriptor;
    let args = [Argument::string(&name)];
    let key = desc.key(&args);
    assert!(
        cache
            .read::<i32, _>(desc, &args, || async { anyhow::bail!("load failure") })
            .await
            .is_err()
    );
    assert!(!cache.registry().registered_keys(desc).await?.contains(&key));
    assert!(
        cache
            .read::<i32, _>(desc, &args, || async { Ok(None) })
            .await?
            .is_none()
    );
    assert!(!cache.registry().registered_keys(desc).await?.contains(&key));
    redis
        .clone()
        .set_ex::<_, _, ()>(&key, "broken JSON", 10)
        .await?;
    assert_eq!(
        cache.read(desc, &args, || async { Ok(Some(19)) }).await?,
        Some(19)
    );
    assert_eq!(redis.clone().get::<_, String>(&key).await?, "19");
    tokio::time::sleep(Duration::from_millis(500)).await;
    assert!(!cache.registry().registered_keys(desc).await?.contains(&key));
    assert_eq!(
        cache.read(desc, &args, || async { Ok(Some(20)) }).await?,
        Some(20)
    );
    cache.shutdown().await?;
    Ok(())
}
