package wiki.chiu.micro.exhibit.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class ExhibitRoutes {

    private static final Logger log = LoggerFactory.getLogger(ExhibitRoutes.class);

    @Bean
    RouterFunction<ServerResponse> exhibitRouter(BlogExhibitHttpHandler handler) {
        return routes(handler);
    }

    public static RouterFunction<ServerResponse> routes(BlogExhibitHttpHandler handler) {
        return withDefaultErrorHandling(
            route()
                .GET("/public/blog/info/{blogId}", handler::getBlogDetail)
                .GET("/public/blog/page/{currentPage}", handler::getPage)
                .POST("/public/blog/secret/{blogId}", handler::getLockedBlog)
                .GET("/public/blog/stat", handler::getVisitStatistics)
                .GET("/public/blog/scores", handler::getScoreBlogs),
            log)
            .build();
    }
}
