package wiki.chiu.micro.exhibit.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.req.ReadTokenReq;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
    Result.class,
    PageAdapter.class,
    ReadTokenReq.class,
    BlogDescriptionVo.class,
    BlogExhibitVo.class,
    BlogHotReadVo.class,
    VisitStatisticsVo.class
})
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
