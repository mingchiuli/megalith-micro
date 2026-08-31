package wiki.chiu.micro.cache.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("megalith.cache")
public class CacheProperties {

    private final Local local = new Local();
    private final SingleFlight singleFlight = new SingleFlight();
    private final Eviction eviction = new Eviction();

    public Local getLocal() {
        return local;
    }

    public SingleFlight getSingleFlight() {
        return singleFlight;
    }

    public Eviction getEviction() {
        return eviction;
    }

    public void validate() {
        if (local.initialCapacity < 0) {
            throw new IllegalArgumentException("megalith.cache.local.initial-capacity must not be negative");
        }
        if (local.maximumEntries <= 0) {
            throw new IllegalArgumentException("megalith.cache.local.maximum-entries must be positive");
        }
        if (local.initialCapacity > local.maximumEntries) {
            throw new IllegalArgumentException(
                "megalith.cache.local.initial-capacity must not exceed maximum-entries");
        }
        if (local.ttlJitter < 0 || local.ttlJitter >= 1) {
            throw new IllegalArgumentException("megalith.cache.local.ttl-jitter must be in [0, 1)");
        }
        requirePositive(singleFlight.waitTimeout, "single-flight.wait-timeout");
        requirePositive(singleFlight.lockEntryTtl, "single-flight.lock-entry-ttl");
        requirePositive(eviction.rabbit.confirmTimeout, "eviction.rabbit.confirm-timeout");
        if (eviction.transport == null) {
            throw new IllegalArgumentException("megalith.cache.eviction.transport must be set");
        }
    }

    private void requirePositive(Duration value, String property) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("megalith.cache." + property + " must be positive");
        }
    }

    public static class Local {

        private int initialCapacity = 512;
        private long maximumEntries = 12_400;
        private double ttlJitter = 0.1;

        public int getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public long getMaximumEntries() {
            return maximumEntries;
        }

        public void setMaximumEntries(long maximumEntries) {
            this.maximumEntries = maximumEntries;
        }

        public double getTtlJitter() {
            return ttlJitter;
        }

        public void setTtlJitter(double ttlJitter) {
            this.ttlJitter = ttlJitter;
        }
    }

    public static class SingleFlight {

        private Duration waitTimeout = Duration.ofSeconds(5);
        private Duration lockEntryTtl = Duration.ofMinutes(30);

        public Duration getWaitTimeout() {
            return waitTimeout;
        }

        public void setWaitTimeout(Duration waitTimeout) {
            this.waitTimeout = waitTimeout;
        }

        public Duration getLockEntryTtl() {
            return lockEntryTtl;
        }

        public void setLockEntryTtl(Duration lockEntryTtl) {
            this.lockEntryTtl = lockEntryTtl;
        }
    }

    public static class Eviction {

        private EvictionTransport transport = EvictionTransport.AUTO;
        private final Rabbit rabbit = new Rabbit();
        private final Redis redis = new Redis();

        public EvictionTransport getTransport() {
            return transport;
        }

        public void setTransport(EvictionTransport transport) {
            this.transport = transport;
        }

        public Rabbit getRabbit() {
            return rabbit;
        }

        public Redis getRedis() {
            return redis;
        }
    }

    public static class Rabbit {

        private String queuePrefix;
        private String exchange;
        private Duration confirmTimeout = Duration.ofSeconds(5);

        public String getQueuePrefix() {
            return queuePrefix;
        }

        public void setQueuePrefix(String queuePrefix) {
            this.queuePrefix = queuePrefix;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public Duration getConfirmTimeout() {
            return confirmTimeout;
        }

        public void setConfirmTimeout(Duration confirmTimeout) {
            this.confirmTimeout = confirmTimeout;
        }
    }

    public static class Redis {

        private String topic = "megalith.cache.evict";

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    public enum EvictionTransport {
        AUTO,
        RABBIT,
        REDIS
    }
}
