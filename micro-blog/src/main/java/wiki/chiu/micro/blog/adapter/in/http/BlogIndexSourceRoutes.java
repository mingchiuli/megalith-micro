package wiki.chiu.micro.blog.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.lang.BlogSnapshot;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({BlogIndexSourceStatus.class, BlogSnapshot.class})
public class BlogIndexSourceRoutes {

    @Bean
    RouterFunction<ServerResponse> indexSourceRouter(BlogIndexSourceHttpHandler handler) {
        return withDefaultErrorHandling(route()
            .GET("/inner/blog/index/status", handler::status)
            .GET("/inner/blog/index/snapshots", handler::snapshots),
            LoggerFactory.getLogger(BlogIndexSourceRoutes.class)).build();
    }
}
