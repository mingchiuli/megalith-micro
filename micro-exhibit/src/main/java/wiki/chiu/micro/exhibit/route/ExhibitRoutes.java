package wiki.chiu.micro.exhibit.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.exhibit.handler.BlogExhibitHttpHandler;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

import java.util.function.Function;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.authInfo;
import static wiki.chiu.micro.common.web.FunctionalWeb.badRequestErrors;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        PageAdapter.class,
        BlogDescriptionVo.class,
        BlogExhibitVo.class,
        BlogHotReadVo.class,
        VisitStatisticsVo.class
})
public class ExhibitRoutes {

    private static final Logger log = LoggerFactory.getLogger(ExhibitRoutes.class);

    @Bean
    RouterFunction<ServerResponse> exhibitRouter(BlogExhibitHttpHandler handler,
                                                 AuthHttpService authHttpService) {
        return routes(handler, request -> authInfo(request, authHttpService));
    }

    public static RouterFunction<ServerResponse> routes(BlogExhibitHttpHandler handler,
                                                        Function<ServerRequest, AuthInfo> authResolver) {
        return route()
                .GET("/public/blog/info/{blogId}", request -> ok(handler.getBlogDetail(
                        pathVariable(request, "blogId", Long::valueOf), authResolver.apply(request))))
                .GET("/public/blog/page/{currentPage}", request -> ok(handler.getPage(
                        pathVariable(request, "currentPage", Integer::valueOf))))
                .GET("/public/blog/secret/{blogId}", request -> ok(handler.getLockedBlog(
                        pathVariable(request, "blogId", Long::valueOf),
                        requiredParam(request, "readToken"))))
                .GET("/public/blog/token/{blogId}", request -> ok(handler.checkReadToken(
                        pathVariable(request, "blogId", Long::valueOf),
                        requiredParam(request, "readToken"))))
                .GET("/public/blog/stat", request -> ok(handler.getVisitStatistics()))
                .GET("/public/blog/scores", request -> ok(handler.getScoreBlogs()))
                .filter(badRequestErrors(log))
                .build();
    }
}
