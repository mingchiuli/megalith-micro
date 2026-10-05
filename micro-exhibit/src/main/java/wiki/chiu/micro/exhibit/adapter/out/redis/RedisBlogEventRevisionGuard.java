package wiki.chiu.micro.exhibit.adapter.out.redis;

import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.domain.BlogEventRevisionKeys;

@Component
public class RedisBlogEventRevisionGuard implements BlogEventRevisionGuard {

    private static final Duration APPLIED_REVISION_TTL = Duration.ofDays(30);

    private final RedissonClient redissonClient;

    public RedisBlogEventRevisionGuard(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void applyIfNewer(Long blogId, long revision, Runnable action) {
        RLock lock = redissonClient.getLock(BlogEventRevisionKeys.lock(blogId));
        try {
            lock.lock();
            RBucket<String> applied = redissonClient.getBucket(BlogEventRevisionKeys.revision(blogId));
            String current = applied.get();
            if (current == null || Long.parseLong(current) < revision) {
                action.run();
                applied.set(Long.toString(revision), APPLIED_REVISION_TTL);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
