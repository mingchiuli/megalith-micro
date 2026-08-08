package wiki.chiu.micro.blog.route;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.handler.BlogHttpHandler;
import wiki.chiu.micro.blog.handler.BlogInternalHttpHandler;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.req.OssDeleteReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.blog.vo.BlogPermissionsVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
  Result.class,
  PageAdapter.class,
  BlogEntityReq.class,
  SensitiveContentReq.class,
  BlogQueryReq.class,
  BlogDownloadReq.class,
  OssDeleteReq.class,
  BlogEntityVo.class,
  BlogDeleteVo.class,
  BlogEditVo.class,
  BlogPermissionsVo.class,
  BlogEntityRpcVo.class,
  BlogSensitiveContentRpcVo.class
})
public class BlogRoutes {

  private static final Logger log = LoggerFactory.getLogger(BlogRoutes.class);

  @Bean
  RouterFunction<ServerResponse> blogRouter(
      BlogHttpHandler blogHandler, BlogInternalHttpHandler internalHandler) {
    return routes(blogHandler, internalHandler);
  }

  public static RouterFunction<ServerResponse> routes(
      BlogHttpHandler blogHandler, BlogInternalHttpHandler internalHandler) {
    return withDefaultErrorHandling(
            route()
                .POST("/sys/blog/save", blogHandler::saveOrUpdate)
                .POST("/sys/blog/delete", blogHandler::deleteBlogs)
                .POST("/sys/blog/lock/{blogId}", blogHandler::setBlogToken)
                .GET("/sys/blog/blogs", blogHandler::getAllBlogs)
                .GET("/sys/blog/deleted", blogHandler::getDeletedBlogs)
                .POST("/sys/blog/recover/{idx}", blogHandler::recoverDeletedBlog)
                .POST("/sys/blog/oss/upload", blogHandler::uploadOss)
                .DELETE("/sys/blog/oss/delete", blogHandler::deleteOss)
                .GET("/sys/blog/download", blogHandler::download)
                .GET("/sys/blog/edit/pull/echo", blogHandler::getEchoDetail)
                .POST("/sys/blog/edit/ticket", blogHandler::issueCollaborationTicket)
                .GET("/inner/blog/count", internalHandler::count)
                .GET("/inner/blog/count/until", internalHandler::countByCreatedGreaterThanEqual)
                .GET("/inner/blog/sensitive/{blogId}", internalHandler::findSensitiveByBlogId)
                .GET("/inner/blog/{blogId}", internalHandler::findById)
                .POST("/inner/blog/batch", internalHandler::findAllById)
                .POST("/inner/blog/page", internalHandler::findPage)
                .POST("/inner/blog/{blogId}", internalHandler::setReadCount),
            log)
        .build();
  }
}
