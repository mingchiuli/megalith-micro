package wiki.chiu.micro.blog.adapter.out.redis;

import static wiki.chiu.micro.common.constant.Const.A_WEEK;
import static wiki.chiu.micro.common.constant.Const.HOT_READ;
import static wiki.chiu.micro.common.constant.Const.QUERY_DELETED;
import static wiki.chiu.micro.common.constant.Const.READ_TOKEN;
import static wiki.chiu.micro.common.constant.Const.RECYCLE_EVENT_PREFIX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.domain.Blog;

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
    public DeletedBlogPage deletedBlogs(
        Long userId, Integer currentPage, Integer size, LocalDateTime expirationCutoff) {
        List<DeletedBlogEntry> live = liveEntries(userId, expirationCutoff);
        long start = (long) (currentPage - 1) * size;
        int from = (int) Math.min(start, live.size());
        int to = (int) Math.min(start + size, live.size());
        return new DeletedBlogPage(List.copyOf(live.subList(from, to)), live.size());
    }

    @Override
    public Optional<DeletedBlogEntry> deletedBlog(
        Long userId, String recoveryId, LocalDateTime expirationCutoff) {
        return liveEntries(userId, expirationCutoff).stream()
            .filter(entry -> entry.recoveryId().equals(recoveryId))
            .findFirst()
            .map(entry -> new DeletedBlogEntry(
                entry.blog().withEventRevision(
                    Optional.ofNullable(entry.blog().eventRevision()).orElse(0L) + 1),
                entry.receipt(), entry.recoveryId()));
    }

    private List<DeletedBlogEntry> liveEntries(Long userId, LocalDateTime cutoff) {
        List<String> stored = redis.opsForList().range(QUERY_DELETED + userId, 0, -1);
        List<DeletedBlogEntry> live = new ArrayList<>();
        List<String> expired = new ArrayList<>();
        for (String receipt : Optional.ofNullable(stored).orElseGet(List::of)) {
            Blog blog = deserialize(receipt);
            if (cutoff.isAfter(blog.updated())) {
                expired.add(receipt);
            } else {
                live.add(new DeletedBlogEntry(blog, receipt, recoveryId(receipt)));
            }
        }
        if (!expired.isEmpty()) {
            redis.execute(RedisScript.of(listDeleteScript, Long.class),
                List.of(QUERY_DELETED + userId), expired.toArray());
        }
        return live;
    }

    private static String recoveryId(String receipt) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(receipt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    @Override
    public void saveDeletedBlog(
        Long userId, String eventId, Blog blog) {
        redis.execute(
            RedisScript.of(recycleScript, Long.class),
            List.of(QUERY_DELETED + userId, RECYCLE_EVENT_PREFIX + eventId),
            jsonMapper.writeValueAsString(blog),
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

    private Blog deserialize(String value) {
        return jsonMapper.readValue(value, Blog.class);
    }

    private String readScript(String name) throws IOException {
        return resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/" + name)
            .getContentAsString(StandardCharsets.UTF_8);
    }
}
