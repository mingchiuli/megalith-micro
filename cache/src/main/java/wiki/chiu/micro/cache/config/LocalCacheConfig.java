package wiki.chiu.micro.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import wiki.chiu.micro.cache.local.LocalCacheExpiry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LocalCacheConfig {

    @Bean("caffeineCache")
    Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(512)// 初始大小
                .maximumSize(12400)// 最大数量
                .expireAfter(new LocalCacheExpiry())//过期时间
                .build();
    }
}
