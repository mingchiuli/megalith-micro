package wiki.chiu.micro.blog.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.constant.Const;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

class RedisBlogRuntimeStoreTest {

    @Test
    void retainsTheStoredBlogShapeAndBothRevisionCounters() throws Exception {
        var redis = mock(StringRedisTemplate.class);
        var json = JsonMapper.builder().build();
        var store = new RedisBlogRuntimeStore(redis, new DefaultResourceLoader(), json);
        store.loadScripts();
        var now = LocalDateTime.of(2026, 11, 7, 10, 0);
        var blog = new Blog(9L, 42L, "title", "description", "content", now, now, 2, "cover", 1L, 3L, 7L,
            List.of(new BlogSensitiveSpan(1, 3, 3)));

        store.saveDeletedBlog(42L, "event", blog);

        var payload = ArgumentCaptor.forClass(String.class);
        verify(redis).execute(ArgumentMatchers.<RedisScript<Long>>any(),
            eq(List.of(Const.QUERY_DELETED + 42L, Const.RECYCLE_EVENT_PREFIX + "event")),
            payload.capture(), eq(Const.A_WEEK));
        var tree = json.readTree(payload.getValue());
        assertThat(tree.get("eventRevision").asLong()).isEqualTo(3L);
        assertThat(tree.get("adminRevision").asLong()).isEqualTo(7L);
        assertThat(tree.has("blog")).isFalse();
        assertThat(json.readValue(payload.getValue(), Blog.class)).isEqualTo(blog);
    }
}
