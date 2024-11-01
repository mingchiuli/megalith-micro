package wiki.chiu.micro.cache.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @Bean(name = "cacheMessageConverter")
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}