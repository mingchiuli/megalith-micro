package wiki.chiu.micro.cache.adapter.out.metrics;

import io.micrometer.core.instrument.MeterRegistry;

import org.jspecify.annotations.Nullable;

import wiki.chiu.micro.cache.application.CacheMetrics;

public class MicrometerCacheMetrics implements CacheMetrics {

    private final @Nullable MeterRegistry registry;

    public MicrometerCacheMetrics(@Nullable MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void request(String result) {
        increment("megalith.cache.requests", "result", result);
    }

    @Override
    public void failure(String operation) {
        increment("megalith.cache.failures", "operation", operation);
    }

    @Override
    public void lockTimeout(String scope) {
        increment("megalith.cache.lock.timeouts", "scope", scope);
    }

    @Override
    public void eviction(String transport, String result) {
        if (registry != null) {
            registry
                .counter("megalith.cache.evictions", "transport", transport, "result", result)
                .increment();
        }
    }

    private void increment(String name, String tagName, String tagValue) {
        if (registry != null) {
            registry.counter(name, tagName, tagValue).increment();
        }
    }
}
