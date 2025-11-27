package wiki.chiu.micro.exhibit.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mingchiuli
 * @create 2023-02-02 10:30 pm
 */
@Configuration(proxyBeanMethods = false)
public class MessageConverterConfig {

    @Bean(name = "jsonMessageConverter")
    JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
