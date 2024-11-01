package wiki.chiu.micro.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;

@AutoConfiguration
public class CacheKeyGeneratorConfig {

    private final ObjectMapper objectMapper;

    public CacheKeyGeneratorConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    CommonCacheKeyGenerator commonCacheKeyGenerator() {
        return new CommonCacheKeyGenerator(objectMapper);
    }
}
