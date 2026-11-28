use anyhow::{Context, Result, ensure};
use cache_rs::{Argument, Cache, CacheConfig, Contract, EvictionConfig, cache};
use redis::aio::ConnectionManager;
use std::{collections::BTreeSet, time::Duration};

const PAGES: Contract = Contract {
    track_keys: true,
    ..Contract::new("interop-pages", 1, Duration::from_secs(1800))
};

struct Pages {
    cache: Cache,
    source: Option<String>,
}

impl Pages {
    #[cache(PAGES)]
    async fn find(&self, id: i64) -> Result<Option<Vec<String>>> {
        Ok(Some(vec![self.source.clone().context("Java L2 miss")?]))
    }
}

#[tokio::test]
#[ignore = "run via the Java CacheRustInteropIntegrationTest fixture"]
async fn exchange_json_leases_and_evictions_with_the_java_starter() -> Result<()> {
    let connection = ConnectionManager::new(redis::Client::open(std::env::var(
        "JAVA_CACHE_INTEROP_REDIS_URL",
    )?)?)
    .await?;
    let cache = Cache::start(
        CacheConfig::default(),
        connection,
        EvictionConfig {
            redis_topic: "interop-topic".into(),
            ..Default::default()
        },
        &[PAGES],
    )
    .await?;
    let descriptor = PAGES.descriptor;
    let arg = [Argument::long(42)];
    let key = descriptor.key(&arg);
    ensure!(
        cache
            .registry()
            .registered_keys(descriptor)
            .await?
            .contains(&key),
        "missing Java lease"
    );
    let mut pages = Pages {
        cache,
        source: None,
    };
    let value = pages.find(42).await?;
    assert_eq!(value, Some(vec!["来自 Java".to_owned()]));
    pages.cache.evict(BTreeSet::from([key])).await?;
    pages.source = Some("来自 Rust".to_owned());
    let value = pages.find(42).await?;
    assert_eq!(value, Some(vec!["来自 Rust".to_owned()]));
    pages.cache.shutdown().await?;
    Ok(())
}
