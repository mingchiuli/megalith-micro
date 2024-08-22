package org.chiu.micro.exhibit.cache.config;

import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class LocalCacheConfig {
    
    @Bean
    Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(128)// 初始大小
                .maximumSize(1024)// 最大数量
                .expireAfter(new LocalCacheExpiry())//过期时间
                .build();
    }
}
