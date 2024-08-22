package org.chiu.micro.exhibit.cache.config;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LocalCacheExpiry implements Expiry<String, Object> {

    private Random random = new Random();

    @Override
    public long expireAfterCreate(String key, Object value, long currentTime) {
        return TimeUnit.MINUTES.toNanos(random.nextInt(30));
    }

    @Override
    public long expireAfterUpdate(String key, Object value, long currentTime, @NonNegative long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(String key, Object value, long currentTime, @NonNegative long currentDuration) {
        return currentDuration;
    }
}
