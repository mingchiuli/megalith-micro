package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@AutoConfiguration
public class LocalCacheConfig {

    private static final Random RANDOM = new Random();

    @Bean("caffeineCache")
    Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(512)// 初始大小
                .maximumSize(12400)// 最大数量
                .expireAfter(Expiry.<String, Object>creating((_, _) -> Duration.ofMinutes(RANDOM.nextInt(30))))//过期时间
                .build();
    }

    @Bean("localLockMap")
    Cache<String, ReentrantLock> localLockMap() {
        return Caffeine.newBuilder()
                .initialCapacity(512)// 初始大小
                .maximumSize(12400)// 最大数量
                .expireAfter(Expiry.<String, Object>creating((_, _) -> Duration.ofMinutes(RANDOM.nextInt(30))))//过期时间
                .build();
    }
}
