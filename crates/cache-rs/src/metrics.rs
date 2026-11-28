use opentelemetry::{KeyValue, global, metrics::Counter};

#[derive(Clone)]
pub(crate) struct Metrics {
    requests: Counter<u64>,
    failures: Counter<u64>,
    timeouts: Counter<u64>,
    evictions: Counter<u64>,
}

impl Metrics {
    pub fn new() -> Self {
        let meter = global::meter("megalith.cache");
        Self {
            requests: meter.u64_counter("megalith.cache.requests").build(),
            failures: meter.u64_counter("megalith.cache.failures").build(),
            timeouts: meter.u64_counter("megalith.cache.lock.timeouts").build(),
            evictions: meter.u64_counter("megalith.cache.evictions").build(),
        }
    }
    pub fn request(&self, result: &'static str) {
        self.requests.add(1, &[KeyValue::new("result", result)]);
    }
    pub fn failure(&self, op: &'static str) {
        self.failures.add(1, &[KeyValue::new("operation", op)]);
    }
    pub fn timeout(&self, scope: &'static str) {
        self.timeouts.add(1, &[KeyValue::new("scope", scope)]);
    }
    pub fn eviction(&self, transport: &'static str, success: bool) {
        self.evictions.add(
            1,
            &[
                KeyValue::new("transport", transport),
                KeyValue::new("result", if success { "success" } else { "failure" }),
            ],
        );
    }
}
