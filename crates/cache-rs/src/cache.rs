use anyhow::{Context, Result, ensure};
use moka::sync::Cache as LockMap;
use redis::{AsyncCommands, aio::ConnectionManager};
use serde::{Serialize, de::DeserializeOwned};
use std::{
    collections::{BTreeSet, HashMap},
    future::Future,
    sync::Arc,
    time::{Duration, Instant},
};
use tokio::{
    sync::Mutex,
    task::JoinHandle,
    time::{sleep, timeout},
};

use crate::{
    Argument, CacheConfig, CacheDescriptor, CacheKeyRegistry, Contract, EvictionConfig,
    local::LocalCache, lock::RemoteLock, metrics::Metrics, registry::millis, transport::Broadcast,
    validate_contracts,
};

pub struct Cache {
    config: CacheConfig,
    contracts: HashMap<CacheDescriptor, Contract>,
    registry: CacheKeyRegistry,
    local: LocalCache,
    locks: LockMap<String, Arc<Mutex<()>>>,
    broadcast: Broadcast,
    workers: Vec<JoinHandle<()>>,
    metrics: Metrics,
}

impl Cache {
    pub async fn start(
        config: CacheConfig,
        redis: ConnectionManager,
        transport: EvictionConfig,
        contracts: &[Contract],
    ) -> Result<Self> {
        config.validate()?;
        validate_contracts(contracts)?;
        let local = LocalCache::new(
            config.initial_capacity,
            config.maximum_entries,
            config.local_ttl_jitter,
        );
        let locks = LockMap::builder()
            .max_capacity(config.maximum_entries)
            .time_to_idle(config.lock_entry_ttl)
            .build();
        let (broadcast, listener) =
            Broadcast::start(transport, redis.clone(), local.clone()).await?;
        let registry = CacheKeyRegistry::new(redis);
        let tracked: Vec<_> = contracts
            .iter()
            .filter(|c| c.track_keys)
            .map(|c| c.descriptor)
            .collect();
        let mut workers = vec![listener];
        if !tracked.is_empty() {
            let cleaner = registry.clone();
            workers.push(tokio::spawn(async move {
                loop {
                    sleep(Duration::from_secs(60)).await;
                    for descriptor in &tracked {
                        if let Err(error) = cleaner.clean(*descriptor).await {
                            tracing::warn!(%error, "cache registry cleanup failed");
                        }
                    }
                }
            }));
        }
        Ok(Self {
            config,
            contracts: contracts.iter().map(|c| (c.descriptor, *c)).collect(),
            registry,
            local,
            locks,
            broadcast,
            workers,
            metrics: Metrics::new(),
        })
    }

    pub async fn ready(&self) -> bool {
        self.broadcast.available().await
            && redis::cmd("PING")
                .query_async::<String>(&mut self.registry.0.clone())
                .await
                .is_ok()
    }

    pub async fn shutdown(mut self) -> Result<()> {
        for worker in self.workers.drain(..) {
            worker.abort();
            let _ = worker.await;
        }
        self.broadcast.unsubscribe().await
    }

    pub fn registry(&self) -> &CacheKeyRegistry {
        &self.registry
    }

    pub async fn read<T, F>(
        &self,
        descriptor: CacheDescriptor,
        args: &[Argument],
        load: impl FnOnce() -> F,
    ) -> Result<Option<T>>
    where
        T: Serialize + DeserializeOwned + Clone + Send + Sync + 'static,
        F: Future<Output = Result<Option<T>>>,
    {
        let contract = *self
            .contracts
            .get(&descriptor)
            .context("unregistered cache contract")?;
        let key = descriptor.key(args);
        if let Some(value) = self.local_hit(&key) {
            return Ok(Some(value));
        }
        let local_lock = self
            .locks
            .get_with(key.clone(), || Arc::new(Mutex::new(())));
        let Ok(_guard) = timeout(self.config.wait_timeout, local_lock.lock()).await else {
            self.metrics.timeout("local");
            self.metrics.request("source");
            return load().await;
        };
        if let Some(value) = self.local_hit(&key) {
            return Ok(Some(value));
        }
        if !contract.track_keys {
            match self.remote_value::<T>(&key).await {
                Ok(Some(value)) => {
                    self.local.put(&key, &value, contract.ttl);
                    self.metrics.request("l2_hit");
                    return Ok(Some(value));
                }
                Err(_) => return self.fallback(contract, &key, load()).await,
                Ok(None) => {}
            }
        }
        let lock = match RemoteLock::acquire(
            self.registry.0.clone(),
            &key,
            self.config.wait_timeout,
        )
        .await
        {
            Ok(Some(lock)) => lock,
            Ok(None) => {
                self.metrics.timeout("remote");
                self.metrics.request("source");
                return load().await;
            }
            Err(_) => {
                self.metrics.failure("remote_lock");
                return self.fallback(contract, &key, load()).await;
            }
        };
        let result = self.load_locked(contract, &key, load).await;
        if let Err(error) = lock.release().await {
            self.metrics.failure("remote_unlock");
            tracing::warn!(%error, "cache unlock failed");
        }
        result
    }

    fn local_hit<T: Clone + Send + Sync + 'static>(&self, key: &str) -> Option<T> {
        let value = self.local.get(key)?;
        self.metrics.request("l1_hit");
        Some(value)
    }

    async fn remote_value<T: DeserializeOwned>(&self, key: &str) -> Result<Option<T>> {
        let mut connection = self.registry.0.clone();
        let bytes: Option<Vec<u8>> = connection
            .get(key)
            .await
            .inspect_err(|_| self.metrics.failure("l2_read"))?;
        let Some(bytes) = bytes else { return Ok(None) };
        match serde_json::from_slice::<Option<T>>(&bytes) {
            Ok(Some(value)) => Ok(Some(value)),
            result => {
                if result.is_err() {
                    self.metrics.failure("deserialize");
                }
                if connection.del::<_, ()>(key).await.is_err() {
                    self.metrics.failure("l2_delete");
                }
                Ok(None)
            }
        }
    }

    async fn fallback<T: Clone + Send + Sync + 'static>(
        &self,
        contract: Contract,
        key: &str,
        load: impl Future<Output = Result<Option<T>>>,
    ) -> Result<Option<T>> {
        self.metrics.request("source");
        let value = load.await?;
        if !contract.track_keys
            && let Some(v) = &value
        {
            self.local.put(key, v, contract.ttl);
        }
        Ok(value)
    }

    async fn load_locked<T, F>(
        &self,
        contract: Contract,
        key: &str,
        load: impl FnOnce() -> F,
    ) -> Result<Option<T>>
    where
        T: Serialize + DeserializeOwned + Clone + Send + Sync + 'static,
        F: Future<Output = Result<Option<T>>>,
    {
        let new = if contract.track_keys {
            match self
                .registry
                .register_lease(contract.descriptor, key, contract.ttl)
                .await
            {
                Ok(lease) => lease.newly_registered,
                Err(_) => {
                    self.metrics.failure("key_registration");
                    return self.fallback(contract, key, load()).await;
                }
            }
        } else {
            false
        };

        let remote = self.remote_value::<T>(key).await;
        let unavailable = remote.is_err();
        let outcome = match remote {
            Err(_) => self.fallback(contract, key, load()).await,
            Ok(Some(value)) => {
                if contract.track_keys {
                    self.tracked_value(contract, key, &value, false).await;
                } else {
                    self.local.put(key, &value, contract.ttl);
                }
                self.metrics.request("l2_hit");
                Ok(Some(value))
            }
            Ok(None) => {
                self.metrics.request("source");
                let value = load().await;
                if let Ok(Some(v)) = &value {
                    if contract.track_keys {
                        self.tracked_value(contract, key, v, true).await;
                    } else {
                        match serde_json::to_vec(v) {
                            Ok(bytes) => {
                                if self
                                    .registry
                                    .0
                                    .clone()
                                    .pset_ex::<_, _, ()>(key, bytes, millis(contract.ttl)?)
                                    .await
                                    .is_err()
                                {
                                    self.metrics.failure("l2_write");
                                }
                            }
                            Err(_) => self.metrics.failure("serialize"),
                        }
                        self.local.put(key, v, contract.ttl);
                    }
                }
                value
            }
        };
        if new
            && (unavailable || !matches!(outcome, Ok(Some(_))))
            && self
                .registry
                .unregister_failed_load(contract.descriptor, key)
                .await
                .is_err()
        {
            self.metrics.failure("key_registration_cleanup");
        }
        outcome
    }

    async fn tracked_value<T: Serialize + Clone + Send + Sync + 'static>(
        &self,
        contract: Contract,
        key: &str,
        value: &T,
        write: bool,
    ) {
        let start = Instant::now();
        let result: Result<()> = async {
            if let Some(lease) = self
                .registry
                .renew_lease(contract.descriptor, key, contract.ttl)
                .await?
            {
                if write
                    && !self
                        .registry
                        .write(key, &serde_json::to_vec(value)?, contract.ttl, lease)
                        .await?
                {
                    return Ok(());
                }
                self.local.put(
                    key,
                    value,
                    contract
                        .ttl
                        .min(lease.remaining.saturating_sub(start.elapsed())),
                );
            }
            Ok(())
        }
        .await;
        if result.is_err() {
            self.metrics.failure(if write {
                "tracked_write"
            } else {
                "key_renewal"
            });
        }
    }

    pub async fn evict(&self, keys: BTreeSet<String>) -> Result<()> {
        if keys.is_empty() {
            return Ok(());
        }
        ensure!(
            keys.iter().all(|k| !k.trim().is_empty()),
            "blank cache eviction key"
        );
        let mut locks = Vec::new();
        let mut result: Result<()> = async {
            for key in &keys {
                let lock =
                    RemoteLock::acquire(self.registry.0.clone(), key, self.config.wait_timeout)
                        .await?
                        .ok_or_else(|| {
                            self.metrics.timeout("evict");
                            anyhow::anyhow!("cache eviction lock timed out")
                        })?;
                locks.push(lock);
            }
            redis::cmd("DEL")
                .arg(keys.iter().collect::<Vec<_>>())
                .query_async::<usize>(&mut self.registry.0.clone())
                .await?;
            for key in &keys {
                self.local.invalidate(key);
            }
            self.broadcast.publish(&keys).await
        }
        .await;
        while let Some(lock) = locks.pop() {
            if let Err(error) = lock.release().await {
                self.metrics.failure("evict_unlock");
                if result.is_ok() {
                    result = Err(error);
                }
            }
        }
        self.metrics.eviction(self.broadcast.name(), result.is_ok());
        result
    }
}

impl Drop for Cache {
    fn drop(&mut self) {
        for task in &self.workers {
            task.abort();
        }
    }
}
