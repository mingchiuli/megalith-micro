package wiki.chiu.micro.cache.application;

/**
 * Records cache request, failure, lock-timeout, and eviction metrics. Implemented by an outbound
 * adapter, so the application stays free of the metrics library.
 */
public interface CacheMetrics {

    void request(String result);

    void failure(String operation);

    void lockTimeout(String scope);

    void eviction(String transport, String result);
}
