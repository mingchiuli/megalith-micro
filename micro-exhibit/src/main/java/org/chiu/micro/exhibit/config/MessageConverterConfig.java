package org.chiu.micro.exhibit.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mingchiuli
 * @create 2023-02-02 10:30 pm
 */
@Configuration
public class MessageConverterConfig {

    @Bean(name = "jsonMessageConverter")
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
