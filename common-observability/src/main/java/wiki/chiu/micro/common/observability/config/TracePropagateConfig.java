package wiki.chiu.micro.common.observability.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@AutoConfiguration
public class TracePropagateConfig {

    @Bean
    public ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }
}
