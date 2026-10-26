package wiki.chiu.micro.search.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.common.page.PageAdapter;

@Component
public class SearchInternalHttpHandler implements SearchHttpService {

    private static final ParameterizedTypeReference<List<BlogReadCountReq>> READ_COUNTS =
        new ParameterizedTypeReference<>() {};

    private final SearchBlogsUseCase searchBlogs;
    private final ValidatedRequest v;

    public SearchInternalHttpHandler(SearchBlogsUseCase searchBlogs, ValidatedRequest v) {
        this.searchBlogs = searchBlogs;
        this.v = v;
    }

    public ServerResponse searchBlogs(ServerRequest request) throws Exception {
        return ok(searchBlogs(SearchRequestConvertor.toBlogSysSearchReq(request)));
    }

    public ServerResponse countBlogs(ServerRequest request) throws Exception {
        return ok(countBlogs(SearchRequestConvertor.toBlogSysCountSearchReq(request)));
    }

    public ServerResponse updateReadCounts(ServerRequest request) throws Exception {
        List<BlogReadCountReq> counts = v.notEmpty(request.body(READ_COUNTS), "counts");
        v.range(counts.size(), 1, 1000, "counts.size");
        for (BlogReadCountReq count : counts) {
            v.notNull(count, "count");
            v.positive(count.blogId(), "blogId");
            v.nonNegative(count.readCount(), "readCount");
        }
        return ok(updateReadCounts(counts));
    }

    public ServerResponse searchAdminBlogs(ServerRequest request) throws Exception {
        return ok(searchAdminBlogs(SearchRequestConvertor.toBlogSysSearchReq(request)));
    }

    public ServerResponse searchRevisions(ServerRequest request) throws Exception {
        BlogRevisionSearchReq req = request.body(BlogRevisionSearchReq.class);
        return ok(searchRevisions(req));
    }

    public ServerResponse publicBlog(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        return ok(publicBlog(blogId));
    }

    public ServerResponse relatedArticles(ServerRequest request) {
        Long blogId = v.positive(pathVariable(request, "blogId", Long::valueOf), "blogId");
        Integer limit = v.range(requiredParam(request, "limit", Integer::valueOf), 1, 20, "limit");
        return ok(relatedArticles(blogId, limit));
    }

    public ServerResponse recommendationPage(ServerRequest request) {
        Integer page = v.positive(requiredParam(request, "page", Integer::valueOf), "page");
        Integer size = v.range(requiredParam(request, "size", Integer::valueOf), 1, 50, "size");
        return ok(recommendationPage(page, size));
    }

    @Override
    public Result<BlogSearchRpcVo> searchBlogs(BlogSysSearchReq req) {
        return Result.success(() -> toResponse(searchBlogs.searchPrivate(toQuery(req))));
    }

    @Override
    public Result<AdminBlogSearchRpcVo> searchAdminBlogs(BlogSysSearchReq req) {
        Page<AdminBlogRead> page = searchBlogs.searchAdmin(toQuery(req));
        return Result.success(
            () ->
                new AdminBlogSearchRpcVo(
                    page.totalElements(),
                    page.pageNumber(),
                    page.pageSize(),
                    page.content().stream()
                        .map(SearchInternalHttpHandler::toResponse)
                        .toList()));
    }

    @Override
    public Result<AdminRevisionSearchRpcVo> searchRevisions(BlogRevisionSearchReq req) {
        v.positive(req.blogId(), "blogId");
        v.positive(req.page(), "page");
        v.range(req.pageSize(), 1, 100, "pageSize");
        v.positive(req.userId(), "userId");
        Page<AdminRevisionRead> page =
            searchBlogs.searchRevisions(
                new RevisionSearchQuery(
                    req.blogId(),
                    req.page(),
                    req.pageSize(),
                    req.keywords(),
                    req.userId(),
                    Boolean.TRUE.equals(req.allData())));
        return Result.success(
            () ->
                new AdminRevisionSearchRpcVo(
                    page.totalElements(),
                    page.pageNumber(),
                    page.pageSize(),
                    page.content().stream()
                        .map(SearchInternalHttpHandler::toResponse)
                        .toList()));
    }

    @Override
    public Result<Long> countBlogs(BlogSysCountSearchReq req) {
        return Result.success(() -> searchBlogs.countPrivate(toQuery(req)));
    }

    @Override
    public Result<Void> updateReadCounts(List<BlogReadCountReq> counts) {
        return Result.success(() -> searchBlogs.updateReadCounts(
            counts.stream().map(count -> new BlogReadCount(count.blogId(), count.readCount())).toList()));
    }

    @Override
    public Result<PublicBlogRpcVo> publicBlog(Long blogId) {
        return Result.success(() -> toResponse(searchBlogs.findPublic(blogId)));
    }

    @Override
    public Result<List<PublicBlogRpcVo>> relatedArticles(Long blogId, Integer limit) {
        List<PublicBlogRead> result = searchBlogs.relatedArticles(blogId, limit);
        return Result.success(
            () -> result.stream().map(SearchInternalHttpHandler::toResponse).toList());
    }

    @Override
    public Result<PageAdapter<PublicBlogRpcVo>> recommendationPage(Integer page, Integer size) {
        Page<PublicBlogRead> result = searchBlogs.recommendationPage(page, size);
        return Result.success(
            () ->
                PageResponse.of(
                    result,
                    result.content().stream()
                        .map(SearchInternalHttpHandler::toResponse)
                        .toList()));
    }

    private static PrivateBlogSearchQuery toQuery(BlogSysSearchReq req) {
        return new PrivateBlogSearchQuery(
            req.page(),
            req.pageSize(),
            req.keywords(),
            req.status(),
            req.createStart(),
            req.createEnd(),
            req.userId(),
            req.allData(),
            Boolean.TRUE.equals(req.draftOnly()));
    }

    private static PrivateBlogSearchQuery toQuery(BlogSysCountSearchReq req) {
        return new PrivateBlogSearchQuery(
            1,
            1,
            req.keywords(),
            req.status(),
            req.createStart(),
            req.createEnd(),
            req.userId(),
            req.allData(),
            false);
    }

    private static BlogSearchRpcVo toResponse(BlogSearchResult result) {
        return BlogSearchRpcVo.builder()
            .ids(result.ids())
            .currentPage(result.page())
            .size(result.pageSize())
            .total(result.total())
            .build();
    }

    private static PublicBlogRpcVo toResponse(PublicBlogRead blog) {
        return new PublicBlogRpcVo(
            blog.id(),
            blog.userId(),
            blog.publishedRevisionId(),
            blog.versionNo(),
            blog.title(),
            blog.description(),
            blog.content(),
            blog.link(),
            blog.readCount(),
            blog.created(),
            blog.publishedAt(),
            blog.offlineAt(),
            blog.accessMode(),
            blog.renderMode(),
            blog.sensitiveContent());
    }

    private static AdminBlogRpcVo toResponse(AdminBlogRead blog) {
        return new AdminBlogRpcVo(
            blog.id(),
            blog.userId(),
            blog.publishedRevisionId(),
            blog.publicationRevision(),
            blog.adminRevision(),
            blog.title(),
            blog.description(),
            blog.content(),
            blog.link(),
            blog.readCount(),
            blog.created(),
            blog.updated(),
            blog.status(),
            blog.accessMode(),
            blog.renderMode(),
            blog.hasDraft(),
            blog.draftNewer(),
            blog.draftRevision(),
            blog.basePublishedRevisionId(),
            blog.draftUpdatedAt());
    }

    private static AdminRevisionRpcVo toResponse(AdminRevisionRead revision) {
        return new AdminRevisionRpcVo(
            revision.id(),
            revision.blogId(),
            revision.versionNo(),
            revision.origin(),
            revision.baseRevisionId(),
            revision.title(),
            revision.description(),
            revision.content(),
            revision.link(),
            revision.renderMode(),
            revision.firstPublishedAt(),
            revision.createdBy(),
            revision.createdAt(),
            revision.currentPublished());
    }
}
