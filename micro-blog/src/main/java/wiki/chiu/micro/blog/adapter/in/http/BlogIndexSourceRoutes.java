package wiki.chiu.micro.blog.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class BlogIndexSourceRoutes {

    @Bean
    RouterFunction<ServerResponse> indexSourceRouter(BlogIndexSourceHttpHandler handler) {
        return withDefaultErrorHandling(route()
            .GET("/inner/blog/index/status", handler::status)
            .GET("/inner/blog/index/snapshots", handler::snapshots),
            LoggerFactory.getLogger(BlogIndexSourceRoutes.class)).build();
    }
}
