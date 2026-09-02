package wiki.chiu.micro.exhibit.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.service.BlogExistenceServiceImpl;

@Testcontainers(disabledWithoutDocker = true)
class RedisBlogExistenceStoreIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = redisContainer();

    private RedissonClient cleanupClient;

    private static GenericContainer<?> redisContainer() {
        GenericContainer<?> result =
            new GenericContainer<>(DockerImageName.parse("redis:8.10.0"));
        result.addExposedPort(6379);
        return result;
    }

    @AfterEach
    void cleanRedis() {
        if (cleanupClient != null) {
            cleanupClient.getKeys().deleteByPattern("blog:existence:v1:*");
            cleanupClient.shutdown();
            cleanupClient = null;
        }
    }

    @Test
    void publishesExactIndexAndAppliesCreateDeleteAndRecovery() {
        RedissonClient redisson = redissonClient();
        cleanupClient = redisson;
        RedisBlogExistenceStore store = new RedisBlogExistenceStore(redisson);

        assertThat(store.lookup(2L)).isEqualTo(BlogExistenceStore.State.UNKNOWN);
        try (BlogExistenceStore.Rebuild rebuild = store.tryBeginRebuild().orElseThrow()) {
            rebuild.addAll(List.of(2L, 5L));
            rebuild.publish();
        }

        assertThat(store.lookup(2L)).isEqualTo(BlogExistenceStore.State.PRESENT);
        assertThat(store.lookup(3L)).isEqualTo(BlogExistenceStore.State.ABSENT);
        store.markAbsent(2L);
        store.markPresent(7L);
        assertThat(store.lookup(2L)).isEqualTo(BlogExistenceStore.State.ABSENT);
        assertThat(store.lookup(7L)).isEqualTo(BlogExistenceStore.State.PRESENT);
        store.markPresent(2L);
        assertThat(store.lookup(2L)).isEqualTo(BlogExistenceStore.State.PRESENT);
        redisson.getBitSet(RedisBlogExistenceStore.BITMAP_KEY).delete();
        assertThat(store.lookup(2L)).isEqualTo(BlogExistenceStore.State.UNKNOWN);
    }

    @Test
    void emptyDatabaseStillPublishesAReadyIndex() {
        RedissonClient redisson = redissonClient();
        cleanupClient = redisson;
        RedisBlogExistenceStore store = new RedisBlogExistenceStore(redisson);

        try (BlogExistenceStore.Rebuild rebuild = store.tryBeginRebuild().orElseThrow()) {
            rebuild.publish();
        }

        assertThat(redisson.getBitSet(RedisBlogExistenceStore.BITMAP_KEY).get(0)).isTrue();
        assertThat(store.lookup(1L)).isEqualTo(BlogExistenceStore.State.ABSENT);
    }

    @Test
    void abandonedStagingIndexNeverBecomesReady() {
        RedissonClient redisson = redissonClient();
        cleanupClient = redisson;
        RedisBlogExistenceStore store = new RedisBlogExistenceStore(redisson);

        try (BlogExistenceStore.Rebuild rebuild = store.tryBeginRebuild().orElseThrow()) {
            rebuild.addAll(List.of(9L));
        }

        assertThat(redisson.getBitSet(RedisBlogExistenceStore.STAGING_KEY).isExists()).isFalse();
        assertThat(store.lookup(9L)).isEqualTo(BlogExistenceStore.State.UNKNOWN);
    }

    @Test
    void onlyOneReplicaCanOwnARebuild() {
        RedissonClient firstClient = redissonClient();
        RedissonClient secondClient = redissonClient();
        cleanupClient = firstClient;
        RedisBlogExistenceStore first = new RedisBlogExistenceStore(firstClient);
        RedisBlogExistenceStore second = new RedisBlogExistenceStore(secondClient);

        try (BlogExistenceStore.Rebuild ignored = first.tryBeginRebuild().orElseThrow()) {
            assertThat(second.tryBeginRebuild()).isEmpty();
        } finally {
            secondClient.shutdown();
        }
    }

    @Test
    void createAndDeleteArrivingDuringRebuildApplyAfterPublication() throws Exception {
        RedissonClient firstClient = redissonClient();
        RedissonClient secondClient = redissonClient();
        cleanupClient = firstClient;
        RedisBlogExistenceStore first = new RedisBlogExistenceStore(firstClient);
        RedisBlogExistenceStore second = new RedisBlogExistenceStore(secondClient);
        publish(first, List.of(1L));
        firstClient.getBucket(RedisBlogExistenceStore.READY_KEY).delete();

        CountDownLatch catalogEntered = new CountDownLatch(1);
        CountDownLatch releaseCatalog = new CountDownLatch(1);
        CountDownLatch mutationsStarted = new CountDownLatch(2);
        BlogCatalog catalog = mock(BlogCatalog.class);
        when(catalog.findIdsAfter(0L, 1000))
            .thenAnswer(
                _ -> {
                    catalogEntered.countDown();
                    assertThat(releaseCatalog.await(5, TimeUnit.SECONDS)).isTrue();
                    return List.of(1L);
                });
        BlogExistenceServiceImpl service =
            new BlogExistenceServiceImpl(first, catalog, 1000, new SimpleMeterRegistry());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var rebuild = executor.submit(service::rebuildIfRequired);
            assertThat(catalogEntered.await(5, TimeUnit.SECONDS)).isTrue();
            var delete =
                executor.submit(
                    () -> {
                        mutationsStarted.countDown();
                        second.markAbsent(1L);
                    });
            var create =
                executor.submit(
                    () -> {
                        mutationsStarted.countDown();
                        second.markPresent(7L);
                    });
            assertThat(mutationsStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(delete.isDone()).isFalse();
            assertThat(create.isDone()).isFalse();

            releaseCatalog.countDown();
            rebuild.get(5, TimeUnit.SECONDS);
            delete.get(5, TimeUnit.SECONDS);
            create.get(5, TimeUnit.SECONDS);
        } finally {
            secondClient.shutdown();
        }

        assertThat(first.lookup(1L)).isEqualTo(BlogExistenceStore.State.ABSENT);
        assertThat(first.lookup(7L)).isEqualTo(BlogExistenceStore.State.PRESENT);
    }

    private void publish(RedisBlogExistenceStore store, List<Long> ids) {
        try (BlogExistenceStore.Rebuild rebuild = store.tryBeginRebuild().orElseThrow()) {
            rebuild.addAll(ids);
            rebuild.publish();
        }
    }

    private RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE);
        config
            .useSingleServer()
            .setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        return Redisson.create(config);
    }
}
