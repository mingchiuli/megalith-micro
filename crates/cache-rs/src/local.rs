use std::{
    any::Any,
    sync::Arc,
    time::{Duration, Instant},
};

use moka::{Expiry, sync::Cache};

#[derive(Clone)]
struct Entry {
    value: Arc<dyn Any + Send + Sync>,
    deadline: Instant,
}

struct EntryExpiry;

impl Expiry<String, Entry> for EntryExpiry {
    fn expire_after_create(
        &self,
        _: &String,
        entry: &Entry,
        created_at: Instant,
    ) -> Option<Duration> {
        Some(entry.deadline.saturating_duration_since(created_at))
    }

    fn expire_after_update(
        &self,
        _: &String,
        entry: &Entry,
        updated_at: Instant,
        _: Option<Duration>,
    ) -> Option<Duration> {
        Some(entry.deadline.saturating_duration_since(updated_at))
    }
}

#[derive(Clone)]
pub(crate) struct LocalCache {
    entries: Cache<String, Entry>,
    jitter: f64,
}

impl LocalCache {
    pub fn new(capacity: usize, maximum: u64, jitter: f64) -> Self {
        Self {
            entries: Cache::builder()
                .initial_capacity(capacity)
                .max_capacity(maximum)
                .expire_after(EntryExpiry)
                .build(),
            jitter,
        }
    }

    pub fn get<T: Clone + Send + Sync + 'static>(&self, key: &str) -> Option<T> {
        let entry = self.entries.get(key)?;
        entry.value.downcast_ref().cloned()
    }

    pub fn put<T: Clone + Send + Sync + 'static>(&self, key: &str, value: &T, ttl: Duration) {
        if ttl.is_zero() {
            return;
        }
        let factor = 1.0 - self.jitter * rand::random::<f64>();
        let deadline = Instant::now() + ttl.mul_f64(factor);
        self.entries.insert(
            key.to_owned(),
            Entry {
                value: Arc::new(value.clone()),
                deadline,
            },
        );
    }

    pub fn invalidate(&self, key: &str) {
        self.entries.invalidate(key);
    }
    pub fn clear(&self) {
        self.entries.invalidate_all();
    }
}
