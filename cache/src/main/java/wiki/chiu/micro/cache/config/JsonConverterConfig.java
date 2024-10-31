package wiki.chiu.micro.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author mingchiuli
 * @since 2023-02-02 10:30 pm
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "megalith", name = "cache.topic", havingValue = "rabbit")
public class JsonConverterConfig {

    private final ObjectMapper objectMapper;

    public JsonConverterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "cacheMessageConverter")
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    CommonCacheKeyGenerator commonCacheKeyGenerator() {
        return new CommonCacheKeyGenerator(objectMapper);
    }
}
