package wiki.chiu.micro.scheduling;

import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

public final class RedisTaskLock {

    private final RedissonClient redissonClient;
    private final TaskLockProperties properties;

    public RedisTaskLock(RedissonClient redissonClient, TaskLockProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }

    public boolean tryRun(String taskName, Runnable task) {
        RLock lock = lock(taskName);
        if (!lock.tryLock()) {
            return false;
        }
        try {
            task.run();
            return true;
        } finally {
            unlock(lock);
        }
    }

    public <T> T run(String taskName, Supplier<T> task) {
        RLock lock = lock(taskName);
        lock.lock();
        try {
            return task.get();
        } finally {
            unlock(lock);
        }
    }

    String lockName(String taskName) {
        Assert.hasText(taskName, "taskName must not be blank");
        return "megalith:" + properties.getEnvironment() + ":task:" + taskName;
    }

    private RLock lock(String taskName) {
        return redissonClient.getLock(lockName(taskName));
    }

    private void unlock(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
