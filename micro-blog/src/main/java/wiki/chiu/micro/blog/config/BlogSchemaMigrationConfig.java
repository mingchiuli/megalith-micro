package wiki.chiu.micro.blog.config;

import javax.sql.DataSource;

import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import wiki.chiu.micro.blog.adapter.out.persistence.BlogSchemaInitializer;

@Configuration(proxyBeanMethods = false)
@Import(DatabaseInitializationDependencyConfigurer.class)
public class BlogSchemaMigrationConfig {

    @Bean
    BlogSchemaInitializer blogSchemaInitializer(DataSource dataSource) {
        return new BlogSchemaInitializer(dataSource);
    }
}
