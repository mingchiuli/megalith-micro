use std::time::Duration;

use anyhow::{Result, ensure};

#[derive(Clone, Debug)]
pub struct CacheConfig {
    pub initial_capacity: usize,
    pub maximum_entries: u64,
    pub local_ttl_jitter: f64,
    pub wait_timeout: Duration,
    pub lock_entry_ttl: Duration,
}

impl Default for CacheConfig {
    fn default() -> Self {
        Self {
            initial_capacity: 512,
            maximum_entries: 12_400,
            local_ttl_jitter: 0.1,
            wait_timeout: Duration::from_secs(5),
            lock_entry_ttl: Duration::from_secs(1800),
        }
    }
}

impl CacheConfig {
    pub(crate) fn validate(&self) -> Result<()> {
        ensure!(
            self.maximum_entries > 0 && self.initial_capacity as u64 <= self.maximum_entries,
            "invalid cache capacity"
        );
        ensure!(
            (0.0..1.0).contains(&self.local_ttl_jitter),
            "invalid cache TTL jitter"
        );
        ensure!(
            !self.wait_timeout.is_zero() && !self.lock_entry_ttl.is_zero(),
            "cache timeouts must be positive"
        );
        Ok(())
    }
}
