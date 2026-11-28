package wiki.chiu.micro.cache.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.adapter.in.aop.CacheAspect;
import wiki.chiu.micro.cache.adapter.out.key.JacksonCacheKeyFactory;
import wiki.chiu.micro.cache.adapter.out.metrics.MicrometerCacheMetrics;
import wiki.chiu.micro.cache.adapter.out.redis.RedisCacheKeyRegistry;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.application.model.LocalCacheEntry;

@Testcontainers
@EnabledIfEnvironmentVariable(named = "CACHE_RUST_INTEROP", matches = "1")
class CacheRustInteropIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    @Test
    void sharesTrackedValuesAndInvalidatesTheJavaReplica() throws Exception {
        String url = "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379);
        Config settings = new Config();
        settings.useSingleServer().setAddress(url);
        var redis = Redisson.create(settings);
        var registry = new RedisCacheKeyRegistry(redis);
        var local = Caffeine.newBuilder().<String, LocalCacheEntry>build();
        var mapper = JsonMapper.builder().build();
        var listener = new RedisCacheEvictMessageListener("interop-topic", redis, mapper, local);
        try {
            var factory = new AspectJProxyFactory(new Pages());
            factory.addAspect(new CacheAspect(redis, mapper, new JacksonCacheKeyFactory(mapper), local,
                Caffeine.newBuilder().<String, ReentrantLock>build(), Duration.ofSeconds(5), 0,
                new MicrometerCacheMetrics(null), registry));
            Pages proxy = factory.getProxy();
            listener.start();
            assertThat(proxy.page(42L)).containsExactly("来自 Java");
            var command = new ProcessBuilder("cargo", "test", "--locked", "-p", "cache-rs",
                "--test", "java_interop", "--", "--ignored").inheritIO();
            command.environment().put("JAVA_CACHE_INTEROP_REDIS_URL", url);
            var process = command.start();
            try {
                assertThat(process.waitFor(10, TimeUnit.MINUTES)).isTrue();
                assertThat(process.exitValue()).isZero();
            } finally {
                if (process.isAlive()) process.destroyForcibly();
            }
            await().atMost(Duration.ofSeconds(5)).until(() -> local.asMap().isEmpty());
            assertThat(proxy.page(42L)).containsExactly("来自 Rust");
        } finally {
            listener.stop();
            registry.close();
            redis.shutdown();
        }
    }

    public static class Pages {

        @Cache(namespace = "interop-pages", trackKeys = true)
        public List<String> page(Long id) {
            return List.of("来自 Java");
        }
    }
}
