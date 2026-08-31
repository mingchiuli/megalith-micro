package wiki.chiu.micro.blog.adapter.out.redis;

import static wiki.chiu.micro.common.lang.Const.A_WEEK;
import static wiki.chiu.micro.common.lang.Const.HOT_READ;
import static wiki.chiu.micro.common.lang.Const.QUERY_DELETED;
import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;
import static wiki.chiu.micro.common.lang.Const.RECYCLE_EVENT_PREFIX;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.convertor.BlogDeleteDtoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityConvertor;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.common.lang.BlogSnapshot;

@Component
public class RedisBlogRuntimeStore implements BlogRuntimeStore {

    private final StringRedisTemplate redis;
    private final ResourceLoader resources;
    private final JsonMapper jsonMapper;
    private String hotBlogsScript;
    private String listDeleteScript;
    private String recycleScript;

    public RedisBlogRuntimeStore(
        StringRedisTemplate redis, ResourceLoader resources, JsonMapper jsonMapper) {
        this.redis = redis;
        this.resources = resources;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    void loadScripts() throws IOException {
        hotBlogsScript = readScript("hot-blogs.lua");
        listDeleteScript = readScript("blog-delete-list.lua");
        recycleScript = readScript("blog-recycle.lua");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, Integer> readCounts(List<Long> blogIds) {
        List<String> result =
            redis.execute(
                RedisScript.of(hotBlogsScript, List.class),
                Collections.singletonList(HOT_READ),
                jsonMapper.writeValueAsString(blogIds.stream().map(String::valueOf).toList()));
        Map<Long, Integer> counts = new HashMap<>();
        for (int i = 0; i < result.size(); i += 2) {
            counts.put(Long.valueOf(result.get(i)), Integer.valueOf(result.get(i + 1)));
        }
        return counts;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeletedBlogPage deletedBlogs(
        Long userId, Integer currentPage, Integer size, LocalDateTime expirationCutoff) {
        String key = QUERY_DELETED + userId;
        List<String> stored = redis.opsForList().range(key, 0, -1);
        List<BlogEntity> all =
            Optional.ofNullable(stored).orElseGet(Collections::emptyList).stream()
                .map(this::deserialize)
                .toList();
        if (all.isEmpty()) {
            return new DeletedBlogPage(0, List.of(), 0);
        }

        int expiredCount =
            (int) all.stream().filter(blog -> expirationCutoff.isAfter(blog.getUpdated())).count();
        int start = (currentPage - 1) * size;
        List<String> result =
            redis.execute(
                RedisScript.of(listDeleteScript, List.class),
                Collections.singletonList(key),
                String.valueOf(expiredCount),
                "-1",
                String.valueOf(size - 1),
                String.valueOf(start));
        List<BlogEntity> blogs =
            result.subList(0, result.size() - 1).stream().map(this::deserialize).toList();
        return new DeletedBlogPage(expiredCount, blogs, Long.parseLong(result.getLast()));
    }

    @Override
    public Optional<DeletedBlogEntry> deletedBlog(Long userId, Integer index) {
        String stored = redis.opsForList().index(QUERY_DELETED + userId, index);
        if (!StringUtils.hasLength(stored)) {
            return Optional.empty();
        }
        BlogDeleteDto deleted = jsonMapper.readValue(stored, BlogDeleteDto.class);
        return Optional.of(new DeletedBlogEntry(BlogEntityConvertor.convertRecover(deleted), stored));
    }

    @Override
    public void saveDeletedBlog(Long userId, String eventId, BlogSnapshot snapshot) {
        redis.execute(
            RedisScript.of(recycleScript, Long.class),
            List.of(QUERY_DELETED + userId, RECYCLE_EVENT_PREFIX + eventId),
            jsonMapper.writeValueAsString(BlogDeleteDtoConvertor.convert(snapshot)),
            A_WEEK);
    }

    @Override
    public void removeDeletedBlog(Long userId, String receipt) {
        redis.opsForList().remove(QUERY_DELETED + userId, 1, receipt);
    }

    @Override
    public void saveReadToken(Long blogId, String token) {
        redis.opsForValue().set(READ_TOKEN + blogId, token, Expiration.from(24, TimeUnit.HOURS));
    }

    private BlogEntity deserialize(String value) {
        return BlogEntityConvertor.convert(jsonMapper.readValue(value, BlogDeleteDto.class));
    }

    private String readScript(String name) throws IOException {
        return resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/" + name)
            .getContentAsString(StandardCharsets.UTF_8);
    }
}
