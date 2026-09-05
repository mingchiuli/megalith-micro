package wiki.chiu.micro.cache.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.aspect.CacheAspect;
import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.handler.impl.RedisCacheEvictor;
import wiki.chiu.micro.cache.key.CacheDescriptor;
import wiki.chiu.micro.cache.key.impl.JacksonCacheKeyFactory;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;
import wiki.chiu.micro.cache.store.RedisCacheKeyRegistry;

@Testcontainers(disabledWithoutDocker = true)
class TrackedCacheIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = redisContainer();

    private static GenericContainer<?> redisContainer() {
        GenericContainer<?> result = new GenericContainer<>(DockerImageName.parse("redis:8.10.0"));
        result.addExposedPort(6379);
        return result;
    }

    @Test
    void evictionSeesAnInFlightLoadAndWaitsForItsKeyLock() throws Exception {
        RedissonClient reader = client();
        RedissonClient consumer = client();
        var local = Caffeine.newBuilder().<String, LocalCacheEntry>build();
        var mapper = JsonMapper.builder().build();
        var properties = new CacheProperties();
        properties.getEviction().getRedis().setTopic("tracked-cache-test");
        var registry = new RedisCacheKeyRegistry(reader);
        var target = new Pages();
        var factory = new AspectJProxyFactory(target);
        factory.addAspect(new CacheAspect(reader, mapper, new JacksonCacheKeyFactory(mapper), local,
            Caffeine.newBuilder().<String, ReentrantLock>build(), properties, new CacheMetrics(null), registry));
        Pages proxy = factory.getProxy();
        var remoteLocal = Caffeine.newBuilder().<String, LocalCacheEntry>build();
        var listener = new RedisCacheEvictMessageListener("tracked-cache-test", reader, mapper, local);
        var evictor = new RedisCacheEvictor(consumer, mapper, remoteLocal, properties, new CacheMetrics(null));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            listener.start();
            var loading = executor.submit(() -> proxy.page(19));
            assertThat(target.entered.await(5, TimeUnit.SECONDS)).isTrue();
            Set<String> keys = registry.registeredKeys(new CacheDescriptor("tracked-pages", 1));
            assertThat(keys).hasSize(1);
            var eviction = executor.submit(() -> evictor.evict(keys));
            assertThatThrownBy(() -> eviction.get(150, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            target.proceed.countDown();
            assertThat(loading.get(10, TimeUnit.SECONDS)).containsExactly("old page");
            eviction.get(10, TimeUnit.SECONDS);

            String key = keys.iterator().next();
            assertThat(reader.getBucket(key).isExists()).isFalse();
            await().atMost(Duration.ofSeconds(5)).until(() -> local.getIfPresent(key) == null);
            assertThat(registry.registeredKeys(new CacheDescriptor("tracked-pages", 1))).isEqualTo(keys);
            target.value = List.of("new page");
            assertThat(proxy.page(19)).containsExactly("new page");
            evictor.evict(registry.registeredKeys(new CacheDescriptor("tracked-pages", 1)));
            await().atMost(Duration.ofSeconds(5)).until(() -> local.getIfPresent(key) == null);

            target.value = List.of();
            assertThat(proxy.page(1)).isEmpty();
            target.value = List.of("first article");
            evictor.evict(registry.registeredKeys(new CacheDescriptor("tracked-pages", 1)));
            await().atMost(Duration.ofSeconds(5)).until(() -> local.asMap().isEmpty());
            assertThat(proxy.page(1)).containsExactly("first article");
        } finally {
            target.proceed.countDown();
            listener.stop();
            consumer.shutdown();
            reader.shutdown();
        }
    }

    private static RedissonClient client() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        return Redisson.create(config);
    }

    public static class Pages {

        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch proceed = new CountDownLatch(1);
        volatile List<String> value = List.of("old page");

        @Cache(namespace = "tracked-pages", trackKeys = true)
        public List<String> page(Integer number) {
            List<String> snapshot = value;
            entered.countDown();
            try {
                if (!proceed.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("test load timed out");
                }
            } catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(failure);
            }
            return snapshot;
        }
    }
}
