package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.auth.web.AuthWeb.authPrincipal;
import static wiki.chiu.micro.common.web.FunctionalWeb.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.blog.application.model.BlogExportPage;
import wiki.chiu.micro.blog.application.model.UploadObject;
import wiki.chiu.micro.blog.application.port.in.BlogAssetService;
import wiki.chiu.micro.blog.application.port.in.BlogCollaborationService;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.result.Result;
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
    private final ValidatedRequest v;
    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
        new ParameterizedTypeReference<>() {
        };

    public BlogHttpHandler(
        BlogService blogService,
        BlogAssetService assetService,
        BlogCollaborationService collaborationService,
        BlogExportService exportService,
        ValidatedRequest v) {
        this.blogService = blogService;
        this.assetService = assetService;
        this.collaborationService = collaborationService;
        this.exportService = exportService;
        this.v = v;
    }

    public ServerResponse createDraftBlog(ServerRequest request) {
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    new BlogIdVo(
                        blogService.createDraftBlog(authInfo.userId()).id())));
    }

    public ServerResponse saveDraft(ServerRequest request) throws Exception {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        BlogEntityReq body = BlogRequestConvertor.toBlogEntityReq(request);
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    blogService.saveDraft(
                        blogId,
                        BlogRequestConvertor.toDraft(body),
                        authInfo.userId(),
                        authInfo.dataPermissions())));
    }

    public ServerResponse saveDraftVersion(ServerRequest request) throws Exception {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        BlogEntityReq body = BlogRequestConvertor.toBlogEntityReq(request);
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toVo(
                        blogService.saveDraftVersion(
                            blogId,
                            BlogRequestConvertor.toDraft(body),
                            authInfo.userId(),
                            authInfo.dataPermissions()))));
    }

    public ServerResponse findVersions(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        Integer currentPage =
            v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
        Integer size = v.range(requiredParam(request, "size", Integer::valueOf), 1, 100, "size");
        String keywords =
            v.maxLength(
                nullableParam(request, "keywords", Function.identity()),
                20,
                "keywords");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () -> {
                    var page =
                        blogService.findVersions(
                            blogId,
                            currentPage,
                            size,
                            keywords,
                            authInfo.userId(),
                            authInfo.dataPermissions());
                    return PageResponse.of(
                        page,
                        page.content().stream()
                            .map(
                                version ->
                                    new BlogVersionSummaryVo(
                                        version.id(),
                                        version.blogId(),
                                        version.versionNo(),
                                        version.origin(),
                                        version.baseRevisionId(),
                                        version.title(),
                                        version.description(),
                                        version.content(),
                                        version.link(),
                                        version.renderMode(),
                                        version.firstPublishedAt(),
                                        version.createdBy(),
                                        version.createdAt(),
                                        version.currentPublished()))
                            .toList());
                }));
    }

    public ServerResponse publish(ServerRequest request) throws Exception {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        PublishBlogReq body = request.body(PublishBlogReq.class);
        v.positive(body.revisionId(), "revisionId");
        v.range(body.accessMode(), 0, 1, "accessMode");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    blogService.publish(
                        blogId,
                        body.revisionId(),
                        body.accessMode(),
                        authInfo.userId(),
                        authInfo.dataPermissions())));
    }

    public ServerResponse offline(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    blogService.offline(
                        blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse online(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    blogService.online(
                        blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse deleteDraft(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    blogService.deleteDraft(
                        blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse findVersion(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        Long revisionId =
            v.positive(pathVariable(request, "revisionId", Long::valueOf), "revisionId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toVo(
                        blogService.findVersion(
                            blogId,
                            revisionId,
                            authInfo.userId(),
                            authInfo.dataPermissions()))));
    }

    public ServerResponse compareVersion(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        Long revisionId =
            v.positive(pathVariable(request, "revisionId", Long::valueOf), "revisionId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toVo(
                        blogService.compareVersion(
                            blogId,
                            revisionId,
                            authInfo.userId(),
                            authInfo.dataPermissions()))));
    }

    public ServerResponse deleteBlogs(ServerRequest request) throws Exception {
        List<Long> ids = v.notEmpty(v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () -> blogService.deleteBatch(ids, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse setBlogToken(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    collaborationService.issueReadToken(
                        blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse getAllBlogs(ServerRequest request) {
        BlogQueryReq query = BlogRequestConvertor.toBlogQueryReq(request);
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toVo(
                        blogService.findAllBlogs(
                            BlogRequestConvertor.toQuery(query),
                            authInfo.userId(),
                            authInfo.dataPermissions()))));
    }

    public ServerResponse getDeletedBlogs(ServerRequest request) {
        Integer currentPage =
            v.positive(requiredParam(request, "currentPage", Integer::valueOf), "currentPage");
        Integer size = v.positive(requiredParam(request, "size", Integer::valueOf), "size");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toDeletedVo(
                        blogService.findDeletedBlogs(
                            currentPage, size, authInfo.userId()))));
    }

    public ServerResponse recoverDeletedBlog(ServerRequest request) {
        Integer idx = v.nonNegative(pathVariable(request, "idx", Integer::valueOf), "idx");
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(Result.success(() -> blogService.recoverDeletedBlog(idx, authInfo.userId())));
    }

    public ServerResponse uploadOss(ServerRequest request) {
        MultipartFile image = multipartFile(request, "image");
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthPrincipal authInfo = authPrincipal(request);
        try {
            UploadObject upload = new UploadObject(image.getBytes());
            return ok(
                Result.success(
                    () ->
                        assetService.upload(
                            upload, blogId, authInfo.userId(), authInfo.dataPermissions())));
        } catch (IOException exception) {
            throw new ValidationException("failed to read uploaded image");
        }
    }

    public ServerResponse deleteOss(ServerRequest request) throws Exception {
        OssDeleteReq body = request.body(OssDeleteReq.class);
        String url = v.notBlank(body.url(), "url");
        Long blogId = body.blogId();
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () -> assetService.delete(url, blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse issueCollaborationTicket(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    collaborationService.issueWebSocketTicket(
                        blogId, authInfo.userId(), authInfo.dataPermissions())));
    }

    public ServerResponse download(ServerRequest request) {
        BlogDownloadReq downloadReq = BlogRequestConvertor.toBlogDownloadReq(request);
        AuthPrincipal authInfo = authPrincipal(request);
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blogs.sql")
            .build(
                (_, response) -> {
                    try (Stream<BlogExportPage> pages =
                        exportService.pages(
                            BlogRequestConvertor.toDownloadQuery(downloadReq),
                            authInfo.userId(),
                            authInfo.dataPermissions())) {
                        OutputStreamWriter writer =
                            new OutputStreamWriter(
                                response.getOutputStream(), StandardCharsets.UTF_8);
                        Iterator<BlogExportPage> export = pages.iterator();
                        while (export.hasNext()) {
                            writer.write(SqlExportMapper.toSql(export.next()));
                        }
                        writer.flush();
                    }
                    return null;
                });
    }

    public ServerResponse getEchoDetail(ServerRequest request) {
        Long blogId = nullableParam(request, "blogId", Long::valueOf);
        if (blogId != null) {
            v.positive(blogId, "blogId");
        }
        AuthPrincipal authInfo = authPrincipal(request);
        return ok(
            Result.success(
                () ->
                    BlogViewMapper.toVo(
                        blogService.findEdit(blogId, authInfo.userId(), authInfo.dataPermissions()))));
    }
}
