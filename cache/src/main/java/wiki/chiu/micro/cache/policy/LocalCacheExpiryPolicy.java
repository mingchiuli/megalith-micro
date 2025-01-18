package wiki.chiu.micro.cache.policy;

import com.github.benmanes.caffeine.cache.Expiry;
import org.jspecify.annotations.NullMarked;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@NullMarked
public class LocalCacheExpiryPolicy implements Expiry<String, Object> {

    private final Random random = new Random();

    @Override
    public long expireAfterCreate(String key, Object value, long currentTime) {
        return TimeUnit.MINUTES.toNanos(random.nextInt(30));
    }

    @Override
    public long expireAfterUpdate(String key, Object value, long currentTime, long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(String key, Object value, long currentTime, long currentDuration) {
        return currentDuration;
    }
}
