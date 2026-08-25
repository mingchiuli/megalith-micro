package wiki.chiu.micro.cache.store;

import java.time.Duration;
import java.util.Objects;

public record LocalCacheEntry(Object value, Duration ttl) {

  public LocalCacheEntry {
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(ttl, "ttl");
    if (ttl.isZero() || ttl.isNegative()) {
      throw new IllegalArgumentException("Local cache TTL must be positive");
    }
  }
}
