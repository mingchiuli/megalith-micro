package wiki.chiu.micro.blog.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.handler.BlogHttpHandler;
import wiki.chiu.micro.blog.handler.BlogInternalHttpHandler;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.*;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        PageAdapter.class,
        BlogEntityReq.class,
        BlogEntityVo.class,
        BlogDeleteVo.class,
        BlogEditVo.class,
        BlogEntityRpcVo.class,
        BlogSensitiveContentRpcVo.class
})
public class BlogRoutes {

    private static final Logger log = LoggerFactory.getLogger(BlogRoutes.class);

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    @Bean
    RouterFunction<ServerResponse> blogRouter(BlogHttpHandler blogHandler,
                                              BlogInternalHttpHandler internalHandler,
                                              AuthHttpService authHttpService,
                                              ValidatedRequest validation) {
        return routes(blogHandler, internalHandler,
                request -> authInfo(request, authHttpService), validation);
    }

    public static RouterFunction<ServerResponse> routes(BlogHttpHandler blogHandler,
                                                        BlogInternalHttpHandler internalHandler,
                                                        Function<ServerRequest, AuthInfo> authResolver,
                                                        ValidatedRequest validation) {
        return route()
                .POST("/sys/blog/save", request -> ok(blogHandler.saveOrUpdate(
                        validation.body(request, BlogEntityReq.class), authResolver.apply(request))))
                .POST("/sys/blog/delete", request -> ok(blogHandler.deleteBlogs(
                        validation.notEmpty(request.body(LONG_LIST), "ids"), authResolver.apply(request))))
                .GET("/sys/blog/lock/{blogId}", request -> ok(blogHandler.setBlogToken(
                        pathVariable(request, "blogId", Long::valueOf), authResolver.apply(request))))
                .GET("/sys/blog/blogs", request -> ok(blogHandler.getAllBlogs(
                        validation.validate(blogQuery(request)), authResolver.apply(request))))
                .GET("/sys/blog/deleted", request -> ok(blogHandler.getDeletedBlogs(
                        requiredParam(request, "currentPage", Integer::valueOf),
                        requiredParam(request, "size", Integer::valueOf),
                        authResolver.apply(request))))
                .GET("/sys/blog/recover/{idx}", request -> ok(blogHandler.recoverDeletedBlog(
                        pathVariable(request, "idx", Integer::valueOf), authResolver.apply(request))))
                .POST("/sys/blog/oss/upload", request -> ok(blogHandler.uploadOss(
                        multipartFile(request, "image"), authResolver.apply(request))))
                .GET("/sys/blog/oss/delete", request ->
                        ok(blogHandler.deleteOss(validation.notBlank(
                                requiredParam(request, "url"), "url"))))
                .GET("/sys/blog/download", request -> downloadResponse(
                        blogHandler, validation.validate(blogDownload(request)), authResolver.apply(request)))
                .GET("/sys/blog/edit/pull/echo", request -> ok(blogHandler.getEchoDetail(
                        nullableParam(request, "blogId", Long::valueOf), authResolver.apply(request))))
                .GET("/inner/blog/count", request -> ok(internalHandler.count()))
                .GET("/inner/blog/count/until", request -> ok(internalHandler.countByCreatedGreaterThanEqual(
                        requiredParam(request, "created", LocalDateTime::parse))))
                .GET("/inner/blog/sensitive/{blogId}", request -> ok(internalHandler.findSensitiveByBlogId(
                        pathVariable(request, "blogId", Long::valueOf))))
                .GET("/inner/blog/{blogId}", request -> ok(internalHandler.findById(
                        pathVariable(request, "blogId", Long::valueOf))))
                .POST("/inner/blog/batch", request -> ok(internalHandler.findAllById(request.body(LONG_LIST))))
                .POST("/inner/blog/page", request -> ok(internalHandler.findPage(
                        requiredParam(request, "pageNo", Integer::valueOf),
                        requiredParam(request, "pageSize", Integer::valueOf))))
                .POST("/inner/blog/{blogId}", request -> ok(internalHandler.setReadCount(
                        pathVariable(request, "blogId", Long::valueOf))))
                .filter(badRequestErrors(log))
                .build();
    }

    private static BlogQueryReq blogQuery(ServerRequest request) {
        return new BlogQueryReq(
                nullableParam(request, "currentPage", Integer::valueOf),
                nullableParam(request, "size", Integer::valueOf),
                nullableParam(request, "keywords", Function.identity()),
                nullableParam(request, "status", Integer::valueOf),
                nullableParam(request, "createStart", LocalDateTime::parse),
                nullableParam(request, "createEnd", LocalDateTime::parse));
    }

    private static BlogDownloadReq blogDownload(ServerRequest request) {
        return new BlogDownloadReq(
                nullableParam(request, "keywords", Function.identity()),
                nullableParam(request, "status", Integer::valueOf),
                nullableParam(request, "createStart", LocalDateTime::parse),
                nullableParam(request, "createEnd", LocalDateTime::parse));
    }

    private static ServerResponse downloadResponse(BlogHttpHandler handler, BlogDownloadReq download,
                                                   AuthInfo authInfo) {
        return ServerResponse.ok().build((request, response) -> {
            handler.download(response, download, authInfo);
            return null;
        });
    }
}
