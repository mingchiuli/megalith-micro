package wiki.chiu.micro.cache.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.jspecify.annotations.Nullable;

public final class CacheMetrics {

  private final @Nullable MeterRegistry registry;

  public CacheMetrics(@Nullable MeterRegistry registry) {
    this.registry = registry;
  }

  public void request(String result) {
    increment("megalith.cache.requests", "result", result);
  }

  public void failure(String operation) {
    increment("megalith.cache.failures", "operation", operation);
  }

  public void lockTimeout(String scope) {
    increment("megalith.cache.lock.timeouts", "scope", scope);
  }

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
