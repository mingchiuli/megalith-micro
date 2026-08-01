package wiki.chiu.micro.blog.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@Component
public class BlogHttpHandler {

    private final BlogService blogService;
    private final AuthHttpService authHttpService;
    private final ValidatedRequest validation;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public BlogHttpHandler(BlogService blogService, AuthHttpService authHttpService,
                           ValidatedRequest validation) {
        this.blogService = blogService;
        this.authHttpService = authHttpService;
        this.validation = validation;
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        BlogEntityReq blog = validation.body(request, BlogEntityReq.class);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.saveOrUpdate(blog, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse deleteBlogs(ServerRequest request) throws Exception {
        List<Long> ids = validation.notEmpty(validation.body(request, LONG_LIST), "ids");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse setBlogToken(ServerRequest request) {
        Long blogId = pathVariable(request, "blogId", Long::valueOf);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.setBlogToken(blogId, authInfo.userId())));
    }

    public ServerResponse getAllBlogs(ServerRequest request) {
        BlogQueryReq query = validation.validate(blogQuery(request));
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findAllBlogs(query, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse getDeletedBlogs(ServerRequest request) {
        Integer currentPage = requiredParam(request, "currentPage", Integer::valueOf);
        Integer size = requiredParam(request, "size", Integer::valueOf);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authInfo.userId())));
    }

    public ServerResponse recoverDeletedBlog(ServerRequest request) {
        Integer idx = pathVariable(request, "idx", Integer::valueOf);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId())));
    }

    public ServerResponse uploadOss(ServerRequest request) {
        MultipartFile image = multipartFile(request, "image");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.uploadOss(image, authInfo.userId())));
    }

    public ServerResponse deleteOss(ServerRequest request) {
        String url = validation.notBlank(requiredParam(request, "url"), "url");
        return ok(Result.success(() -> blogService.deleteOss(url)));
    }

    public ServerResponse download(ServerRequest request) {
        BlogDownloadReq download = validation.validate(blogDownload(request));
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ServerResponse.ok().build((servletRequest, response) -> {
            blogService.download(response, download, authInfo.userId(), authInfo.roles());
            return null;
        });
    }

    public ServerResponse getEchoDetail(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findEdit(blogId, authInfo.userId(), authInfo.roles())));
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

}
