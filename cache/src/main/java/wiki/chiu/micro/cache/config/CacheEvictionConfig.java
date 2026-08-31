package wiki.chiu.micro.cache.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

import wiki.chiu.micro.cache.handler.CacheEvictor;

@AutoConfiguration
@AutoConfigureAfter({CacheEvictRabbitConfig.class, CacheEvictRedisConfig.class})
public class CacheEvictionConfig {

    @Bean
    CacheEvictionInfrastructure cacheEvictionInfrastructure(CacheEvictor cacheEvictor) {
        return new CacheEvictionInfrastructure();
    }

    static final class CacheEvictionInfrastructure {
    }
}
