package wiki.chiu.micro.search.route;

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
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.handler.BlogSearchHttpHandler;
import wiki.chiu.micro.search.handler.SearchInternalHttpHandler;
import wiki.chiu.micro.search.vo.BlogDocumentVo;

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
  RouterFunction<ServerResponse> searchRouter(
      BlogSearchHttpHandler searchHandler, SearchInternalHttpHandler internalHandler) {
    return routes(searchHandler, internalHandler);
  }

  public static RouterFunction<ServerResponse> routes(
      BlogSearchHttpHandler searchHandler, SearchInternalHttpHandler internalHandler) {
    return withDefaultErrorHandling(
            route()
                .GET("/search/public/blog", searchHandler::searchBlogs)
                .POST("/inner/blog/search", internalHandler::searchBlogs)
                .POST("/inner/blog/count", internalHandler::countBlogs)
                .POST("/inner/blog/{blogId}/views", internalHandler::addReadCount),
            log)
        .build();
  }
}
