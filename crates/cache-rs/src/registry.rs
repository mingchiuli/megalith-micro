use std::{collections::BTreeSet, time::Duration};

use anyhow::{Result, bail, ensure};
use redis::{AsyncCommands, aio::ConnectionManager};

use crate::CacheDescriptor;

const LEASE: &str = include_str!("../script/cache-key-lease.lua");
const PRUNE: &str = include_str!("../script/cache-key-prune.lua");
const TRACKED_WRITE: &str = include_str!("../script/cache-tracked-write.lua");

#[derive(Debug, Clone, Copy)]
pub struct CacheKeyLease {
    pub newly_registered: bool,
    pub expires_at_epoch_millis: u64,
    pub remaining: Duration,
}

#[derive(Clone)]
pub struct CacheKeyRegistry(pub(crate) ConnectionManager);

impl CacheKeyRegistry {
    pub fn new(connection: ConnectionManager) -> Self {
        Self(connection)
    }

    pub async fn register(&self, descriptor: CacheDescriptor, key: &str) -> Result<bool> {
        Ok(self
            .register_lease(descriptor, key, Duration::from_secs(1800))
            .await?
            .newly_registered)
    }

    pub async fn register_lease(
        &self,
        descriptor: CacheDescriptor,
        key: &str,
        ttl: Duration,
    ) -> Result<CacheKeyLease> {
        self.lease(descriptor, key, ttl, "register")
            .await?
            .ok_or_else(|| anyhow::anyhow!("missing cache registration"))
    }

    pub async fn renew_lease(
        &self,
        descriptor: CacheDescriptor,
        key: &str,
        ttl: Duration,
    ) -> Result<Option<CacheKeyLease>> {
        self.lease(descriptor, key, ttl, "renew").await
    }

    async fn lease(
        &self,
        descriptor: CacheDescriptor,
        key: &str,
        ttl: Duration,
        operation: &str,
    ) -> Result<Option<CacheKeyLease>> {
        ensure!(!ttl.is_zero(), "registry TTL must be positive");
        let result: Vec<u64> = redis::cmd("EVAL")
            .arg(LEASE)
            .arg(1)
            .arg(descriptor.directory())
            .arg(key)
            .arg(millis(ttl)?)
            .arg(operation)
            .query_async(&mut self.0.clone())
            .await?;
        match result.as_slice() {
            [] => Ok(None),
            &[new, expires, remaining] => Ok(Some(CacheKeyLease {
                newly_registered: new == 1,
                expires_at_epoch_millis: expires,
                remaining: Duration::from_millis(remaining),
            })),
            _ => bail!("unexpected Redis cache lease response"),
        }
    }

    pub async fn registered_keys(&self, descriptor: CacheDescriptor) -> Result<BTreeSet<String>> {
        self.prune(descriptor, "list").await
    }

    pub async fn clean(&self, descriptor: CacheDescriptor) -> Result<()> {
        self.prune(descriptor, "prune").await?;
        Ok(())
    }

    async fn prune(
        &self,
        descriptor: CacheDescriptor,
        operation: &str,
    ) -> Result<BTreeSet<String>> {
        Ok(redis::cmd("EVAL")
            .arg(PRUNE)
            .arg(1)
            .arg(descriptor.directory())
            .arg(operation)
            .query_async(&mut self.0.clone())
            .await?)
    }

    pub async fn unregister_failed_load(
        &self,
        descriptor: CacheDescriptor,
        key: &str,
    ) -> Result<()> {
        Ok(self
            .0
            .clone()
            .zrem::<_, _, ()>(descriptor.directory(), key)
            .await?)
    }

    pub(crate) async fn write(
        &self,
        key: &str,
        value: &[u8],
        ttl: Duration,
        lease: CacheKeyLease,
    ) -> Result<bool> {
        Ok(redis::cmd("EVAL")
            .arg(TRACKED_WRITE)
            .arg(1)
            .arg(key)
            .arg(value)
            .arg(millis(ttl)?)
            .arg(lease.expires_at_epoch_millis)
            .query_async(&mut self.0.clone())
            .await?)
    }
}

pub(crate) fn millis(ttl: Duration) -> Result<u64> {
    Ok(ttl.as_millis().max(1).try_into()?)
}
