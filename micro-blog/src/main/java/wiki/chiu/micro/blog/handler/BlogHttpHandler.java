package wiki.chiu.micro.blog.handler;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.*;

import java.io.IOException;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.blog.converter.BlogRequestConverter;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.req.OssDeleteReq;
import wiki.chiu.micro.blog.service.BlogAssetService;
import wiki.chiu.micro.blog.service.BlogCollaborationService;
import wiki.chiu.micro.blog.service.BlogExportService;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.service.UploadObject;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.web.ValidatedRequest;

/**
 * @author mingchiuli
 * @create 2022-12-01 9:28 pm
 */
@Component
public class BlogHttpHandler {

  private final BlogService blogService;
  private final BlogAssetService assetService;
  private final BlogCollaborationService collaborationService;
  private final BlogExportService exportService;
  private final AuthHttpService authHttpService;
  private final ValidatedRequest v;
  private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
      new ParameterizedTypeReference<>() {};

  public BlogHttpHandler(
      BlogService blogService,
      BlogAssetService assetService,
      BlogCollaborationService collaborationService,
      BlogExportService exportService,
      AuthHttpService authHttpService,
      ValidatedRequest v) {
    this.blogService = blogService;
    this.assetService = assetService;
    this.collaborationService = collaborationService;
    this.exportService = exportService;
    this.authHttpService = authHttpService;
    this.v = v;
  }

  public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
    BlogEntityReq blog = BlogRequestConverter.toBlogEntityReq(request);
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(() -> blogService.saveOrUpdate(blog, authInfo.userId(), authInfo.roles())));
  }

  public ServerResponse deleteBlogs(ServerRequest request) throws Exception {
    List<Long> ids = v.notEmpty(v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(() -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.roles())));
  }

  public ServerResponse setBlogToken(ServerRequest request) {
    Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(
            () ->
                collaborationService.issueReadToken(blogId, authInfo.userId(), authInfo.roles())));
  }

  public ServerResponse getAllBlogs(ServerRequest request) {
    BlogQueryReq query = BlogRequestConverter.toBlogQueryReq(request);
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(() -> blogService.findAllBlogs(query, authInfo.userId(), authInfo.roles())));
  }

  public ServerResponse getDeletedBlogs(ServerRequest request) {
    Integer currentPage =
        v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
    Integer size = v.positive(requiredParam(request, "size", Integer::valueOf), "size");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(() -> blogService.findDeletedBlogs(currentPage, size, authInfo.userId())));
  }

  public ServerResponse recoverDeletedBlog(ServerRequest request) {
    Integer idx = v.nonNegative(pathVariable(request, "idx", Integer::valueOf), "idx");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId())));
  }

  public ServerResponse uploadOss(ServerRequest request) {
    MultipartFile image = multipartFile(request, "image");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    try {
      UploadObject upload = new UploadObject(image.getBytes());
      return ok(Result.success(() -> assetService.upload(upload, authInfo.userId())));
    } catch (IOException exception) {
      throw new ValidationException("failed to read uploaded image");
    }
  }

  public ServerResponse deleteOss(ServerRequest request) throws Exception {
    OssDeleteReq body = request.body(OssDeleteReq.class);
    String url = v.notBlank(body.url(), "url");
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(Result.success(() -> assetService.delete(url, authInfo.userId())));
  }

  public ServerResponse issueCollaborationTicket(ServerRequest request) {
    Long blogId = nullableParam(request, "blogId", Long::valueOf);
    if (blogId != null) {
      v.positive(blogId, "blogId");
    }
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(
            () ->
                collaborationService.issueWebSocketTicket(
                    blogId, authInfo.userId(), authInfo.roles())));
  }

  public ServerResponse download(ServerRequest request) {
    BlogDownloadReq downloadReq = BlogRequestConverter.toBlogDownloadReq(request);
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blogs.sql")
        .build(
            (servletRequest, response) -> {
              exportService.write(
                  downloadReq, authInfo.userId(), authInfo.roles(), response.getOutputStream());
              return null;
            });
  }

  public ServerResponse getEchoDetail(ServerRequest request) {
    Long blogId = nullableParam(request, "blogId", Long::valueOf);
    if (blogId != null) {
      v.positive(blogId, "blogId");
    }
    AuthPrincipal authInfo = authPrincipal(request, authHttpService);
    return ok(
        Result.success(() -> blogService.findEdit(blogId, authInfo.userId(), authInfo.roles())));
  }
}
