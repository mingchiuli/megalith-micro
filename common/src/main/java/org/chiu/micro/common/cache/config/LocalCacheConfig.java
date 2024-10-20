package org.chiu.micro.common.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.chiu.micro.common.cache.CacheAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(CacheAspect.class)
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
