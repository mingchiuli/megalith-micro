package wiki.chiu.micro.exhibit.adapter.out.redis;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.redisson.api.RBitSet;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

@Component
public class RedisBlogExistenceStore implements BlogExistenceStore {

    static final String BITMAP_KEY = "blog:existence:v1:bitmap";
    static final String READY_KEY = "blog:existence:v1:ready";
    static final String STAGING_KEY = "blog:existence:v1:staging";
    static final String LOCK_KEY = "blog:existence:v1:rebuild-lock";

    private final RedissonClient redisson;

    public RedisBlogExistenceStore(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public State lookup(Long blogId) {
        try {
            RBucket<String> ready = redisson.getBucket(READY_KEY);
            String generation = ready.get();
            if (generation == null) {
                return State.UNKNOWN;
            }
            RBitSet bitmap = redisson.getBitSet(BITMAP_KEY);
            if (!bitmap.isExists()) {
                return State.UNKNOWN;
            }
            boolean present = bitmap.get(blogId);
            if (!bitmap.isExists() || !Objects.equals(generation, ready.get())) {
                return State.UNKNOWN;
            }
            return present ? State.PRESENT : State.ABSENT;
        } catch (RedisException failure) {
            return State.UNKNOWN;
        }
    }

    @Override
    public void markPresent(Long blogId) {
        mark(blogId, true);
    }

    @Override
    public void markAbsent(Long blogId) {
        mark(blogId, false);
    }

    private void mark(Long blogId, boolean present) {
        RLock lock = redisson.getReadWriteLock(LOCK_KEY).readLock();
        lock.lock();
        try {
            RBucket<String> ready = redisson.getBucket(READY_KEY);
            RBitSet bitmap = redisson.getBitSet(BITMAP_KEY);
            if (!ready.isExists() || !bitmap.isExists()) {
                ready.delete();
                return;
            }
            bitmap.set(blogId, present);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Optional<Rebuild> tryBeginRebuild() {
        RLock lock = redisson.getReadWriteLock(LOCK_KEY).writeLock();
        if (!lock.tryLock()) {
            return Optional.empty();
        }

        boolean handedOff = false;
        try {
            if (isReady()) {
                return Optional.empty();
            }
            redisson.getBucket(READY_KEY).delete();
            RBitSet staging = redisson.getBitSet(STAGING_KEY);
            staging.delete();
            staging.set(0, true);
            handedOff = true;
            return Optional.of(new RedisRebuild(lock, staging));
        } finally {
            if (!handedOff && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean isReady() {
        return redisson.getBucket(READY_KEY).get() != null
            && redisson.getBitSet(BITMAP_KEY).isExists();
    }

    private final class RedisRebuild implements Rebuild {

        private final RLock lock;
        private final RBitSet staging;
        private boolean published;
        private boolean closed;

        private RedisRebuild(RLock lock, RBitSet staging) {
            this.lock = lock;
            this.staging = staging;
        }

        @Override
        public void addAll(List<Long> blogIds) {
            long[] indexes = blogIds.stream().mapToLong(Long::longValue).toArray();
            staging.set(indexes, true);
        }

        @Override
        public void publish() {
            if (published || closed) {
                throw new IllegalStateException("Blog existence rebuild is already completed");
            }
            staging.rename(BITMAP_KEY);
            redisson.<String>getBucket(READY_KEY).set(UUID.randomUUID().toString());
            published = true;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                if (!published) {
                    staging.delete();
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
