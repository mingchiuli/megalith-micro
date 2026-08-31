package wiki.chiu.micro.cache.listener;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.aspect.CacheAspect;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.handler.impl.RabbitCacheEvictor;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictor;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.cache.key.impl.JacksonCacheKeyFactory;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

@EnabledIfEnvironmentVariable(named = "CACHE_LOCAL_MIDDLEWARE", matches = "true")
class LocalMiddlewareCacheBenchmarkTest {

    private static final String TEST_PREFIX = "cache-benchmark-";
    private static final int KEY_SAMPLES = 100_000;
    private static final int L1_SAMPLES = 100_000;
    private static final int L2_SAMPLES = 2_000;
    private static final int REDIS_SAMPLES = 5_000;
    private static final int EVICTION_SAMPLES = 50;
    private static final int CONCURRENT_CALLERS = 128;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final CacheKeyFactory keyFactory = new JacksonCacheKeyFactory(jsonMapper);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private volatile Object sink;

    @AfterEach
    void closeMeterRegistry() {
        meterRegistry.close();
    }

    @Test
    void measuresTheCompleteLocalCachePath() throws Exception {
        RedissonClient firstRedis = redisson();
        RedissonClient secondRedis = redisson();
        String runId = UUID.randomUUID().toString();
        try {
            cleanup(firstRedis);
            benchmarkKeys();
            benchmarkRedisBaseline(firstRedis, runId);
            benchmarkReadPath(firstRedis);
            verifyDistributedSingleFlight(firstRedis);
            verifyTtl(firstRedis);
            verifyLoadEvictionOrdering(firstRedis, secondRedis, runId);
            benchmarkRedisEviction(firstRedis, secondRedis, runId);
            benchmarkRabbitEviction(firstRedis, runId);
        } finally {
            cleanup(firstRedis);
            secondRedis.shutdown();
            firstRedis.shutdown();
        }
    }

    private void benchmarkKeys() {
        CacheDescriptor descriptor = new CacheDescriptor(TEST_PREFIX + "key", 1);
        for (int index = 0; index < 20_000; index++) {
            sink = keyFactory.generate(descriptor, 42L, "ROLE_USER");
        }
        Metric metric =
            measure(
                "key.sha256_canonical_json",
                KEY_SAMPLES,
                index -> sink = keyFactory.generate(descriptor, (long) index, "ROLE_USER"));
        print(metric);
    }

    private void benchmarkRedisBaseline(RedissonClient redisson, String runId) {
        String key = TEST_PREFIX + "redis-direct:" + runId;
        var bucket = redisson.getBucket(key, StringCodec.INSTANCE);
        bucket.set("value", Duration.ofMinutes(5));
        for (int index = 0; index < 1_000; index++) {
            sink = bucket.get();
        }
        Metric metric =
            measure("redis.direct_get", REDIS_SAMPLES, _ -> sink = bucket.get());
        assertThat(sink).isEqualTo("value");
        print(metric);
    }

    private void benchmarkReadPath(RedissonClient redisson) {
        deleteNamespace(redisson, CacheFixture.READ_NAMESPACE);
        Replica l1Replica = replica(redisson, new CacheFixture());
        assertThat(l1Replica.proxy().load("l1")).startsWith("source:");
        for (int index = 0; index < 20_000; index++) {
            sink = l1Replica.proxy().load("l1");
        }
        Metric l1 =
            measure("cache.aspect_l1_hit", L1_SAMPLES, _ -> sink = l1Replica.proxy().load("l1"));
        assertThat(l1Replica.target().readLoads()).isEqualTo(1);
        print(l1);

        deleteNamespace(redisson, CacheFixture.READ_NAMESPACE);
        for (int index = 0; index < L2_SAMPLES; index++) {
            String key =
                keyFactory.generate(
                    new CacheDescriptor(CacheFixture.READ_NAMESPACE, 1), "l2-" + index);
            redisson
                .getBucket(key, StringCodec.INSTANCE)
                .set("\"remote\"", Duration.ofMinutes(5));
        }
        Replica l2Replica = replica(redisson, new CacheFixture());
        Metric l2 =
            measure(
                "cache.aspect_l2_hit",
                L2_SAMPLES,
                index -> sink = l2Replica.proxy().load("l2-" + index));
        assertThat(l2Replica.target().readLoads()).isZero();
        assertThat(sink).isEqualTo("remote");
        print(l2);
    }

    private void verifyDistributedSingleFlight(RedissonClient redisson) throws Exception {
        deleteNamespace(redisson, CacheFixture.SINGLE_FLIGHT_NAMESPACE);
        CacheFixture firstTarget = new CacheFixture(Duration.ofMillis(75));
        CacheFixture secondTarget = new CacheFixture(Duration.ofMillis(75));
        Replica first = replica(redisson, firstTarget);
        Replica second = replica(redisson, secondTarget);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>(CONCURRENT_CALLERS);
        long started;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int index = 0; index < CONCURRENT_CALLERS; index++) {
                CacheFixture proxy = index % 2 == 0 ? first.proxy() : second.proxy();
                futures.add(
                    executor.submit(
                        () -> {
                            start.await();
                            return proxy.singleFlight("shared");
                        }));
            }
            started = System.nanoTime();
            start.countDown();
            for (Future<String> future : futures) {
                sink = future.get(10, TimeUnit.SECONDS);
            }
        }
        long elapsed = System.nanoTime() - started;
        Set<String> results =
            futures.stream()
                .map(
                    future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                .collect(java.util.stream.Collectors.toSet());
        int sourceLoads = firstTarget.singleFlightLoads() + secondTarget.singleFlightLoads();
        assertThat(sourceLoads).isEqualTo(1);
        assertThat(results).hasSize(1);
        System.out.printf(
            Locale.ROOT,
            "CACHE_BENCHMARK_CHECK distributed_single_flight callers=%d replicas=2 source_loads=%d total_ms=%.3f%n",
            CONCURRENT_CALLERS,
            sourceLoads,
            elapsed / 1_000_000.0);
    }

    private void verifyTtl(RedissonClient redisson) {
        deleteNamespace(redisson, CacheFixture.TTL_NAMESPACE);
        CacheFixture target = new CacheFixture();
        Replica replica = replica(redisson, target);
        long started = System.nanoTime();
        sink = replica.proxy().shortTtl("ttl");
        String key =
            keyFactory.generate(new CacheDescriptor(CacheFixture.TTL_NAMESPACE, 1), "ttl");
        long initialTtl = redisson.getBucket(key, StringCodec.INSTANCE).remainTimeToLive();
        await(() -> !redisson.getBucket(key, StringCodec.INSTANCE).isExists(), Duration.ofSeconds(2));
        long expiredAfter = System.nanoTime() - started;
        sink = replica.proxy().shortTtl("ttl");
        assertThat(initialTtl).isPositive().isLessThanOrEqualTo(400);
        assertThat(target.ttlLoads()).isEqualTo(2);
        System.out.printf(
            Locale.ROOT,
            "CACHE_BENCHMARK_CHECK ttl configured_ms=400 initial_redis_ttl_ms=%d observed_expiry_ms=%.3f source_loads=%d%n",
            initialTtl,
            expiredAfter / 1_000_000.0,
            target.ttlLoads());
    }

    private void verifyLoadEvictionOrdering(
        RedissonClient firstRedis, RedissonClient secondRedis, String runId) throws Exception {
        deleteNamespace(firstRedis, CacheFixture.RACE_NAMESPACE);
        String topic = TEST_PREFIX + "race-topic:" + runId;
        CacheFixture target = new CacheFixture();
        Replica loadingReplica = replica(firstRedis, target);
        Replica listeningReplica = replica(secondRedis, new CacheFixture());
        CacheProperties properties = properties();
        properties.getEviction().getRedis().setTopic(topic);
        RedisCacheEvictMessageListener listener =
            new RedisCacheEvictMessageListener(
                topic, secondRedis, jsonMapper, listeningReplica.localCache());
        RedisCacheEvictor evictor =
            new RedisCacheEvictor(
                firstRedis,
                jsonMapper,
                loadingReplica.localCache(),
                properties,
                new CacheMetrics(meterRegistry));
        String key =
            keyFactory.generate(new CacheDescriptor(CacheFixture.RACE_NAMESPACE, 1), "shared");
        listener.start();
        long started = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> load = executor.submit(() -> loadingReplica.proxy().race("shared"));
            assertThat(target.raceStarted().await(5, TimeUnit.SECONDS)).isTrue();
            listeningReplica.localCache().put(key, localEntry());

            Future<?> eviction = executor.submit(() -> evictor.evict(Set.of(key)));
            LockSupport.parkNanos(Duration.ofMillis(25).toNanos());
            assertThat(eviction).as("eviction must wait for the in-flight load lock").isNotDone();

            target.releaseRace();
            assertThat(load.get(5, TimeUnit.SECONDS)).startsWith("source:shared:");
            eviction.get(5, TimeUnit.SECONDS);
            await(
                () ->
                    loadingReplica.localCache().getIfPresent(key) == null
                        && listeningReplica.localCache().getIfPresent(key) == null,
                Duration.ofSeconds(5));
            assertThat(firstRedis.getBucket(key, StringCodec.INSTANCE).isExists()).isFalse();
            assertThat(loadingReplica.proxy().race("shared")).endsWith(":2");
            System.out.printf(
                Locale.ROOT,
                "CACHE_BENCHMARK_CHECK load_evict_race eviction_waited=true l1_replicas=2 l2_absent=true source_loads=%d total_ms=%.3f%n",
                target.raceLoads(),
                (System.nanoTime() - started) / 1_000_000.0);
        } finally {
            target.releaseRace();
            listener.stop();
            firstRedis.getReliableTopic(topic, StringCodec.INSTANCE).delete();
        }
    }

    private void benchmarkRedisEviction(
        RedissonClient firstRedis, RedissonClient secondRedis, String runId) {
        String topic = TEST_PREFIX + "redis-topic:" + runId;
        Cache<String, LocalCacheEntry> publisherLocal = localCache();
        Cache<String, LocalCacheEntry> replicaLocal = localCache();
        CacheProperties properties = properties();
        properties.getEviction().getRedis().setTopic(topic);
        RedisCacheEvictMessageListener listener =
            new RedisCacheEvictMessageListener(topic, secondRedis, jsonMapper, replicaLocal);
        listener.start();
        try {
            RedisCacheEvictor evictor =
                new RedisCacheEvictor(
                    firstRedis,
                    jsonMapper,
                    publisherLocal,
                    properties,
                    new CacheMetrics(meterRegistry));
            Metric metric =
                measure(
                    "eviction.redis_reliable_topic_e2e",
                    EVICTION_SAMPLES,
                    index -> {
                        String key = TEST_PREFIX + "redis-evict:" + runId + ":" + index;
                        putForEviction(firstRedis, publisherLocal, replicaLocal, key);
                        evictor.evict(Set.of(key));
                        await(() -> replicaLocal.getIfPresent(key) == null, Duration.ofSeconds(5));
                        assertThat(firstRedis.getBucket(key, StringCodec.INSTANCE).isExists()).isFalse();
                    });
            print(metric);
        } finally {
            listener.stop();
            firstRedis.getReliableTopic(topic, StringCodec.INSTANCE).delete();
        }
    }

    private void benchmarkRabbitEviction(RedissonClient redisson, String runId) {
        CachingConnectionFactory connectionFactory = rabbitConnectionFactory();
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        String exchangeName = TEST_PREFIX + "rabbit-exchange." + runId;
        FanoutExchange exchange = new FanoutExchange(exchangeName, false, true);
        Queue firstQueue = new Queue(exchangeName + ".first", false, true, true);
        Queue secondQueue = new Queue(exchangeName + ".second", false, true, true);
        admin.declareExchange(exchange);
        admin.declareQueue(firstQueue);
        admin.declareQueue(secondQueue);
        admin.declareBinding(BindingBuilder.bind(firstQueue).to(exchange));
        admin.declareBinding(BindingBuilder.bind(secondQueue).to(exchange));

        Cache<String, LocalCacheEntry> publisherLocal = localCache();
        Cache<String, LocalCacheEntry> firstLocal = localCache();
        Cache<String, LocalCacheEntry> secondLocal = localCache();
        SimpleMessageListenerContainer firstContainer =
            listenerContainer(connectionFactory, firstQueue, firstLocal);
        SimpleMessageListenerContainer secondContainer =
            listenerContainer(connectionFactory, secondQueue, secondLocal);
        firstContainer.start();
        secondContainer.start();
        await(
            () ->
                firstContainer.getActiveConsumerCount() == 1
                    && secondContainer.getActiveConsumerCount() == 1,
            Duration.ofSeconds(10));

        try {
            CacheProperties properties = properties();
            properties.getEviction().getRabbit().setExchange(exchangeName);
            RabbitCacheEvictor evictor =
                new RabbitCacheEvictor(
                    rabbitTemplate(connectionFactory),
                    redisson,
                    publisherLocal,
                    properties,
                    new CacheMetrics(meterRegistry));
            Metric metric =
                measure(
                    "eviction.rabbit_confirmed_fanout_e2e",
                    EVICTION_SAMPLES,
                    index -> {
                        String key = TEST_PREFIX + "rabbit-evict:" + runId + ":" + index;
                        putForEviction(redisson, publisherLocal, firstLocal, key);
                        secondLocal.put(key, localEntry());
                        evictor.evict(Set.of(key));
                        await(
                            () ->
                                firstLocal.getIfPresent(key) == null
                                    && secondLocal.getIfPresent(key) == null,
                            Duration.ofSeconds(5));
                        assertThat(redisson.getBucket(key, StringCodec.INSTANCE).isExists()).isFalse();
                    });
            print(metric);
        } finally {
            secondContainer.stop();
            firstContainer.stop();
            admin.deleteQueue(secondQueue.getName());
            admin.deleteQueue(firstQueue.getName());
            admin.deleteExchange(exchangeName);
            connectionFactory.destroy();
        }
    }

    private Replica replica(RedissonClient redisson, CacheFixture target) {
        Cache<String, LocalCacheEntry> local = localCache();
        CacheAspect aspect =
            new CacheAspect(
                redisson,
                jsonMapper,
                keyFactory,
                local,
                Caffeine.newBuilder().maximumSize(10_000).build(),
                properties(),
                new CacheMetrics(meterRegistry));
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return new Replica(factory.getProxy(), target, local);
    }

    private Cache<String, LocalCacheEntry> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(200_000)
            .expireAfter(Expiry.<String, LocalCacheEntry>creating((_, entry) -> entry.ttl()))
            .build();
    }

    private CacheProperties properties() {
        CacheProperties result = new CacheProperties();
        result.getLocal().setMaximumEntries(200_000);
        result.getLocal().setTtlJitter(0);
        return result;
    }

    private RedissonClient redisson() {
        String host = environment("CACHE_LOCAL_REDIS_HOST", "127.0.0.1");
        String port = environment("CACHE_LOCAL_REDIS_PORT", "6379");
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        String password = environment("CACHE_LOCAL_REDIS_PASSWORD", "");
        if (!password.isBlank()) {
            config.setPassword(password);
        }
        return Redisson.create(config);
    }

    private CachingConnectionFactory rabbitConnectionFactory() {
        String host = environment("CACHE_LOCAL_RABBIT_HOST", "127.0.0.1");
        int port = Integer.parseInt(environment("CACHE_LOCAL_RABBIT_PORT", "5672"));
        CachingConnectionFactory result = new CachingConnectionFactory(host, port);
        result.setUsername(environment("CACHE_LOCAL_RABBIT_USERNAME", "guest"));
        result.setPassword(environment("CACHE_LOCAL_RABBIT_PASSWORD", "guest"));
        result.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        result.setPublisherReturns(true);
        return result;
    }

    private RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate result = new RabbitTemplate(connectionFactory);
        result.setMandatory(true);
        result.setMessageConverter(messageConverter());
        return result;
    }

    private SimpleMessageListenerContainer listenerContainer(
        CachingConnectionFactory connectionFactory,
        Queue queue,
        Cache<String, LocalCacheEntry> localCache) {
        MessageListenerAdapter adapter =
            new MessageListenerAdapter(new RabbitCacheEvictMessageListener(localCache));
        adapter.setMessageConverter(messageConverter());
        SimpleMessageListenerContainer result =
            new SimpleMessageListenerContainer(connectionFactory);
        result.setAcknowledgeMode(AcknowledgeMode.AUTO);
        result.setQueueNames(queue.getName());
        result.setMessageListener(adapter);
        return result;
    }

    private JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter(jsonMapper, CacheEvictionMessage.class.getPackageName());
    }

    private void putForEviction(
        RedissonClient redisson,
        Cache<String, LocalCacheEntry> firstLocal,
        Cache<String, LocalCacheEntry> secondLocal,
        String key) {
        firstLocal.put(key, localEntry());
        secondLocal.put(key, localEntry());
        redisson.getBucket(key, StringCodec.INSTANCE).set("value", Duration.ofMinutes(1));
    }

    private LocalCacheEntry localEntry() {
        return new LocalCacheEntry("value", Duration.ofMinutes(1));
    }

    private void deleteNamespace(RedissonClient redisson, String namespace) {
        redisson.getKeys().deleteByPattern(namespace + ":*");
    }

    private void cleanup(RedissonClient redisson) {
        redisson.getKeys().deleteByPattern(TEST_PREFIX + "*");
        redisson.getKeys().deleteByPattern("megalithRemoteLock:" + TEST_PREFIX + "*");
    }

    private void await(BooleanSupplier condition, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) {
                throw new AssertionError("Condition was not met within " + timeout);
            }
            LockSupport.parkNanos(100_000);
        }
    }

    private Metric measure(String name, int samples, IndexedOperation operation) {
        long[] durations = new long[samples];
        long started = System.nanoTime();
        for (int index = 0; index < samples; index++) {
            long operationStarted = System.nanoTime();
            operation.run(index);
            durations[index] = System.nanoTime() - operationStarted;
        }
        return Metric.from(name, durations, System.nanoTime() - started);
    }

    private void print(Metric metric) {
        System.out.printf(
            Locale.ROOT,
            "CACHE_BENCHMARK_METRIC name=%s samples=%d total_ms=%.3f throughput_ops_s=%.1f mean_us=%.3f p50_us=%.3f p95_us=%.3f p99_us=%.3f max_us=%.3f%n",
            metric.name(),
            metric.samples(),
            metric.totalNanos() / 1_000_000.0,
            metric.throughput(),
            metric.meanNanos() / 1_000.0,
            metric.p50Nanos() / 1_000.0,
            metric.p95Nanos() / 1_000.0,
            metric.p99Nanos() / 1_000.0,
            metric.maxNanos() / 1_000.0);
    }

    private String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    @FunctionalInterface
    private interface IndexedOperation {

        void run(int index);
    }

    private record Replica(
        CacheFixture proxy, CacheFixture target, Cache<String, LocalCacheEntry> localCache) {
    }

    private record Metric(
        String name,
        int samples,
        long totalNanos,
        double meanNanos,
        long p50Nanos,
        long p95Nanos,
        long p99Nanos,
        long maxNanos) {

        static Metric from(String name, long[] durations, long totalNanos) {
            long[] sorted = durations.clone();
            Arrays.sort(sorted);
            double mean = Arrays.stream(sorted).average().orElseThrow();
            return new Metric(
                name,
                sorted.length,
                totalNanos,
                mean,
                percentile(sorted, 0.50),
                percentile(sorted, 0.95),
                percentile(sorted, 0.99),
                sorted[sorted.length - 1]);
        }

        double throughput() {
            return samples * 1_000_000_000.0 / totalNanos;
        }

        private static long percentile(long[] values, double percentile) {
            int index = (int) Math.ceil(percentile * values.length) - 1;
            return values[Math.max(0, index)];
        }
    }

    static class CacheFixture {

        static final String READ_NAMESPACE = TEST_PREFIX + "read";
        static final String SINGLE_FLIGHT_NAMESPACE = TEST_PREFIX + "single-flight";
        static final String TTL_NAMESPACE = TEST_PREFIX + "ttl";
        static final String RACE_NAMESPACE = TEST_PREFIX + "race";

        private final AtomicInteger readLoads = new AtomicInteger();
        private final AtomicInteger singleFlightLoads = new AtomicInteger();
        private final AtomicInteger ttlLoads = new AtomicInteger();
        private final AtomicInteger raceLoads = new AtomicInteger();
        private final CountDownLatch raceStarted = new CountDownLatch(1);
        private final CountDownLatch raceRelease = new CountDownLatch(1);
        private final Duration delay;

        CacheFixture() {
            this(Duration.ZERO);
        }

        CacheFixture(Duration delay) {
            this.delay = delay;
        }

        @wiki.chiu.micro.cache.annotation.Cache(
            namespace = READ_NAMESPACE,
            ttl = 5,
            timeUnit = TimeUnit.MINUTES)
        public String load(String id) {
            return "source:" + id + ":" + readLoads.incrementAndGet();
        }

        @wiki.chiu.micro.cache.annotation.Cache(
            namespace = SINGLE_FLIGHT_NAMESPACE,
            ttl = 5,
            timeUnit = TimeUnit.MINUTES)
        public String singleFlight(String id) {
            if (!delay.isZero()) {
                LockSupport.parkNanos(delay.toNanos());
            }
            return "source:" + id + ":" + singleFlightLoads.incrementAndGet();
        }

        @wiki.chiu.micro.cache.annotation.Cache(
            namespace = TTL_NAMESPACE,
            ttl = 400,
            timeUnit = TimeUnit.MILLISECONDS)
        public String shortTtl(String id) {
            return "source:" + id + ":" + ttlLoads.incrementAndGet();
        }

        @wiki.chiu.micro.cache.annotation.Cache(
            namespace = RACE_NAMESPACE,
            ttl = 5,
            timeUnit = TimeUnit.MINUTES)
        public String race(String id) {
            raceStarted.countDown();
            try {
                if (!raceRelease.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Race fixture was not released");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Race fixture was interrupted", e);
            }
            return "source:" + id + ":" + raceLoads.incrementAndGet();
        }

        int readLoads() {
            return readLoads.get();
        }

        int singleFlightLoads() {
            return singleFlightLoads.get();
        }

        int ttlLoads() {
            return ttlLoads.get();
        }

        CountDownLatch raceStarted() {
            return raceStarted;
        }

        void releaseRace() {
            raceRelease.countDown();
        }

        int raceLoads() {
            return raceLoads.get();
        }
    }
}
