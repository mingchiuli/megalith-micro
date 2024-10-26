package wiki.chiu.micro.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.cache.utils.JsonUtils;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author mingchiuli
 * @create 2023-02-02 10:30 pm
 */
@AutoConfiguration
public class CacheConverterConfig {

    private final ObjectMapper objectMapper;

    public CacheConverterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "cacheMessageConverter")
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    CommonCacheKeyGenerator commonCacheKeyGenerator() {
        return new CommonCacheKeyGenerator(jsonUtils());
    }

    @Bean
    JsonUtils jsonUtils() {
        return new JsonUtils(objectMapper);
    }
}
