package wiki.chiu.micro.cache.handler;

/** A registry lease measured at Redis, with a relative lifetime for local monotonic clocks. */
public record CacheKeyLease(boolean newlyRegistered, long expiresAtEpochMillis, long remainingMillis) {
}
