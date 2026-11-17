package wiki.chiu.micro.auth.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.LoginChannel.EMAIL;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.LoginChannel.PHONE;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification.ACCEPTED;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification.ATTEMPTS_EXCEEDED;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification.MISMATCH;
import static wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification.NOT_FOUND;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.core.io.DefaultResourceLoader;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class RedisLoginCodeStoreIntegrationTest {
    @Container
    static final GenericContainer<?> REDIS = redisContainer();

    private static GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:8.10.0"));
        container.addExposedPorts(6379);
        return container;
    }

    @Test
    void concurrentReservationAndConsumptionHaveOneWinner() throws Exception {
        var config = new Config().setCodec(StringCodec.INSTANCE);
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        var redis = Redisson.create(config);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var store = new RedisLoginCodeStore(redis, new DefaultResourceLoader());
            store.loadScripts();
            String principal = UUID.randomUUID().toString();
            var start = new CountDownLatch(1);
            var reservations = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 32; i++) {
                reservations.add(executor.submit(() -> {
                    start.await();
                    return store.reserve(PHONE, principal, "123456", UUID.randomUUID().toString());
                }));
            }
            start.countDown();
            int winners = 0;
            for (var result : reservations) if (result.get(5, TimeUnit.SECONDS)) winners++;
            assertThat(winners).isEqualTo(1);
            var consumed = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 32; i++) {
                consumed.add(executor.submit(() -> store.verify(PHONE, principal, "123456", 3) == ACCEPTED));
            }
            winners = 0;
            for (var result : consumed) if (result.get(5, TimeUnit.SECONDS)) winners++;
            assertThat(winners).isEqualTo(1);
        } finally {
            redis.shutdown();
        }
    }

    @Test
    void attemptsKeepTheDeadlineAndCancellationCannotDeleteANewerReservation() throws Exception {
        var config = new Config().setCodec(StringCodec.INSTANCE);
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        var redis = Redisson.create(config);
        try {
            var store = new RedisLoginCodeStore(redis, new DefaultResourceLoader());
            store.loadScripts();
            String principal = UUID.randomUUID().toString();
            assertThat(store.reserve(EMAIL, principal, "AbCd", "first")).isTrue();
            String key = wiki.chiu.micro.common.constant.Const.EMAIL_KEY + principal;
            long ttl = redis.getBucket(key).remainTimeToLive();
            assertThat(store.verify(EMAIL, principal, "bad", 2)).isEqualTo(MISMATCH);
            assertThat(redis.getBucket(key).remainTimeToLive()).isLessThanOrEqualTo(ttl);
            assertThat(store.verify(EMAIL, principal, "bad", 2)).isEqualTo(ATTEMPTS_EXCEEDED);
            assertThat(store.verify(EMAIL, principal, "AbCd", 2)).isEqualTo(NOT_FOUND);
            assertThat(store.reserve(EMAIL, principal, "AbCd", "second")).isTrue();
            store.cancel(EMAIL, principal, "first");
            assertThat(store.verify(EMAIL, principal, "aBcD", 2)).isEqualTo(ACCEPTED);
            store.reserve(EMAIL, principal, "x", "third");
            redis.getBucket(key).expire(java.time.Duration.ofMillis(30));
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(2))
                .until(() -> !redis.getBucket(key).isExists());
            assertThat(store.verify(EMAIL, principal, "x", 2)).isEqualTo(NOT_FOUND);
        } finally {
            redis.shutdown();
        }
    }
}
