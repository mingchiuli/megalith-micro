package wiki.chiu.micro.blog.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.application.service.BlogServiceImpl;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.exception.MissException;

@Testcontainers
class RecycleBinIntegrationTest {
    @Container
    static final GenericContainer<?> REDIS = redisContainer();

    private static GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:8.10.0"));
        container.addExposedPorts(6379);
        return container;
    }

    @Test
    void stableRecoverySurvivesPagingOutOfOrderExpiryAndListChanges() throws Exception {
        var connection = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connection.afterPropertiesSet();
        try {
            var redis = new StringRedisTemplate(connection);
            var store = new RedisBlogRuntimeStore(redis, new DefaultResourceLoader(), JsonMapper.builder().build());
            store.loadScripts();
            var writer = mock(BlogWriter.class);
            var service = new BlogServiceImpl(null, store, writer, null, null, null, null);
            var now = LocalDateTime.now();
            for (long id = 1; id <= 25; id++) {
                store.saveDeletedBlog(42L, UUID.randomUUID().toString(), blog(id, now));
                if (id == 10) store.saveDeletedBlog(42L, UUID.randomUUID().toString(), blog(99L, now.minusDays(8)));
            }
            var page = service.findDeletedBlogs(2, 20, 42L);
            assertThat(page.totalElements()).isEqualTo(25);
            var selected = page.content().getFirst();
            assertThat(selected.id()).isEqualTo(21L);
            assertThat(selected.recoveryId()).matches("[0-9a-f]{64}");
            assertThat(store.deletedBlog(43L, selected.recoveryId(), now.minusDays(7))).isEmpty();
            var first = store.deletedBlogs(42L, 1, 20, now.minusDays(7)).entries().getFirst();
            store.removeDeletedBlog(42L, first.receipt());
            service.recoverDeletedBlog(selected.recoveryId(), 42L);
            var captured = org.mockito.ArgumentCaptor.forClass(Blog.class);
            verify(writer).recoverDeletedBlog(captured.capture(), any());
            assertThat(captured.getValue().id()).isEqualTo(21L);
            assertThatThrownBy(() -> service.recoverDeletedBlog(selected.recoveryId(), 42L))
                .isInstanceOf(MissException.class);
            assertThat(store.deletedBlogs(42L, 1, 100, now.minusDays(7)).entries())
                .noneMatch(entry -> entry.blog().id() == 99L);

            var remaining = store.deletedBlogs(42L, 1, 20, now.minusDays(7)).entries().getFirst();
            doThrow(new IllegalStateException("database failed")).when(writer).recoverDeletedBlog(any(), any());
            assertThatThrownBy(() -> service.recoverDeletedBlog(remaining.recoveryId(), 42L))
                .hasMessage("database failed");
            assertThat(store.deletedBlog(42L, remaining.recoveryId(), now.minusDays(7))).isPresent();
            assertThat(service.findDeletedBlogs(100, 20, 42L).content()).isEmpty();
        } finally {
            connection.destroy();
        }
    }

    private static Blog blog(long id, LocalDateTime updated) {
        return new Blog(id, 42L, "title", "description", "content", updated, updated, 0, "", 0L, 1L);
    }
}
