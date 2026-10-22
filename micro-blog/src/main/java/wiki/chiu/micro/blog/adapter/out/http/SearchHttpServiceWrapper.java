package wiki.chiu.micro.blog.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.api.SearchHttpService;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;

@Component
public class SearchHttpServiceWrapper implements BlogSearchGateway, BlogStatisticsGateway {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    @Override
    public BlogSearchResult searchBlogs(BlogSearchQuery query) {
        return SearchRpcMapper.toResult(
            RemoteResult.requireSuccess(
                () -> searchHttpService.searchBlogs(SearchRpcMapper.toSearchReq(query))));
    }

    @Override
    public Page<BlogListItem> searchAdmin(BlogSearchQuery query) {
        AdminBlogSearchRpcVo result =
            RemoteResult.requireSuccess(
                () -> searchHttpService.searchAdminBlogs(SearchRpcMapper.toSearchReq(query)));
        List<BlogListItem> content =
            result.content().stream()
                .map(
                    blog ->
                        new BlogListItem(
                            blog.id(),
                            blog.title(),
                            blog.description(),
                            blog.content(),
                            blog.link(),
                            blog.readCount(),
                            0,
                            blog.created(),
                            blog.updated(),
                            blog.status(),
                            blog.hasDraft(),
                            blog.draftNewer()))
                .toList();
        long total = result.total();
        int size = result.size();
        int pages = (int) ((total + size - 1) / size);
        return new Page<>(
            content,
            total,
            result.currentPage(),
            size,
            result.currentPage() == 1,
            result.currentPage() >= pages,
            total == 0,
            pages);
    }

    @Override
    public Page<BlogVersionSummary> searchVersions(
        Long blogId,
        int page,
        int pageSize,
        String keywords,
        long userId,
        boolean allData) {
        AdminRevisionSearchRpcVo result =
            RemoteResult.requireSuccess(
                () ->
                    searchHttpService.searchRevisions(
                        new BlogRevisionSearchReq(
                            blogId, page, pageSize, keywords, userId, allData)));
        List<BlogVersionSummary> content =
            result.content().stream()
                .map(
                    revision ->
                        new BlogVersionSummary(
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
                            revision.currentPublished()))
                .toList();
        int totalPages = (int) ((result.total() + result.size() - 1) / result.size());
        return new Page<>(
            content,
            result.total(),
            result.currentPage(),
            result.size(),
            result.currentPage() == 1,
            result.currentPage() >= totalPages,
            result.total() == 0,
            totalPages);
    }

    @Override
    public long countBlogs(BlogSearchQuery query) {
        return RemoteResult.requireSuccess(
            () -> searchHttpService.countBlogs(SearchRpcMapper.toCountReq(query)));
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        RemoteResult.requireSuccess(
            () ->
                searchHttpService.updateReadCounts(
                    counts.stream()
                        .map(count -> new BlogReadCountReq(count.blogId(), count.readCount()))
                        .toList()));
    }
}
