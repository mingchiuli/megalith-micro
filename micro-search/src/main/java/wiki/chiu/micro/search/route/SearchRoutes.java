package wiki.chiu.micro.search.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.handler.BlogSearchHttpHandler;
import wiki.chiu.micro.search.handler.SearchInternalHttpHandler;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.badRequestErrors;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        PageAdapter.class,
        BlogSysSearchReq.class,
        BlogSysCountSearchReq.class,
        BlogDocumentVo.class
})
public class SearchRoutes {

    private static final Logger log = LoggerFactory.getLogger(SearchRoutes.class);

    @Bean
    RouterFunction<ServerResponse> searchRouter(BlogSearchHttpHandler searchHandler,
                                                SearchInternalHttpHandler internalHandler,
                                                ValidatedRequest validation) {
        return routes(searchHandler, internalHandler, validation);
    }

    public static RouterFunction<ServerResponse> routes(BlogSearchHttpHandler searchHandler,
                                                        SearchInternalHttpHandler internalHandler,
                                                        ValidatedRequest validation) {
        return route()
                .GET("/search/public/blog", request -> ok(searchHandler.searchBlogs(
                        requiredParam(request, "currentPage", Integer::valueOf),
                        requiredParam(request, "allInfo", Boolean::valueOf),
                        validation.size(requiredParam(request, "keywords"), 1, 20, "keywords"))))
                .POST("/inner/blog/search", request ->
                        ok(internalHandler.searchBlogs(validation.body(request, BlogSysSearchReq.class))))
                .POST("/inner/blog/count", request ->
                        ok(internalHandler.countBlogs(validation.body(request, BlogSysCountSearchReq.class))))
                .POST("/inner/blog/read", request -> ok(internalHandler.addReadCount(
                        requiredParam(request, "id", Long::valueOf))))
                .filter(badRequestErrors(log))
                .build();
    }
}
