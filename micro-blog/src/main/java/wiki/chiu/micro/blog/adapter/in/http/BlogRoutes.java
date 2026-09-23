package wiki.chiu.micro.blog.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration(proxyBeanMethods = false)
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
                .GET("/inner/blog/ids", internalHandler::findIdsAfter)
                .GET("/inner/blog/sensitive/{blogId}", internalHandler::findSensitiveByBlogId)
                .GET("/inner/blog/page", internalHandler::findPage)
                .GET("/inner/blog/{blogId}", internalHandler::findById)
                .POST("/inner/blog/batch", internalHandler::findAllById)
                .POST("/inner/blog/{blogId}/views", internalHandler::setReadCount),
            log)
            .build();
    }
}
