package wiki.chiu.micro.blog.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.blog.converter.BlogRequestConverter;
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

import java.util.List;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@Component
public class BlogHttpHandler {

    private final BlogService blogService;
    private final AuthHttpService authHttpService;
    private final ValidatedRequest v;
    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public BlogHttpHandler(BlogService blogService, AuthHttpService authHttpService,
                           ValidatedRequest v) {
        this.blogService = blogService;
        this.authHttpService = authHttpService;
        this.v = v;
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        BlogEntityReq blog = BlogRequestConverter.toBlogEntityReq(request);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.saveOrUpdate(blog, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse deleteBlogs(ServerRequest request) throws Exception {
        List<Long> ids = v.notEmpty(
                v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse setBlogToken(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.setBlogToken(blogId, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse getAllBlogs(ServerRequest request) {
        BlogQueryReq query = BlogRequestConverter.toBlogQueryReq(request);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findAllBlogs(query, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse getDeletedBlogs(ServerRequest request) {
        Integer currentPage = v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
        Integer size = v.positive(requiredParam(request, "size", Integer::valueOf), "size");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authInfo.userId())));
    }

    public ServerResponse recoverDeletedBlog(ServerRequest request) {
        Integer idx = v.nonNegative(pathVariable(request, "idx", Integer::valueOf), "idx");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId())));
    }

    public ServerResponse uploadOss(ServerRequest request) {
        MultipartFile image = multipartFile(request, "image");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.uploadOss(image, authInfo.userId())));
    }

    public ServerResponse deleteOss(ServerRequest request) {
        String url = v.notBlank(requiredParam(request, "url"), "url");
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.deleteOss(url, authInfo.userId())));
    }

    public ServerResponse issueCollaborationTicket(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.issueCollaborationTicket(
                blogId, authInfo.userId(), authInfo.roles())));
    }

    public ServerResponse download(ServerRequest request) {
        BlogDownloadReq downloadReq = BlogRequestConverter.toBlogDownloadReq(request);
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ServerResponse.ok().build((servletRequest, response) -> {
            blogService.download(response, downloadReq, authInfo.userId(), authInfo.roles());
            return null;
        });
    }

    public ServerResponse getEchoDetail(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthInfo authInfo = authInfo(request, authHttpService);
        return ok(Result.success(() -> blogService.findEdit(blogId, authInfo.userId(), authInfo.roles())));
    }

}
