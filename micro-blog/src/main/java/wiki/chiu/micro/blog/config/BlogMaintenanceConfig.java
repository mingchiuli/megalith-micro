package wiki.chiu.micro.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;

@Configuration(proxyBeanMethods = false)
public class BlogMaintenanceConfig {

    @Bean
    BlogMaintenanceMode blogMaintenanceMode(
        @Value("${megalith.blog.maintenance.read-only:false}") boolean readOnly) {
        return new BlogMaintenanceMode(readOnly);
    }
}
