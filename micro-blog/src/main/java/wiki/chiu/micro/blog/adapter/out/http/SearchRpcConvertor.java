package wiki.chiu.micro.blog.adapter.out.http;

import java.util.List;
import java.util.Objects;

import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogRevisionSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;

/**
 * Translates between the search service payloads and this service's own search model.
 */
public final class SearchRpcConvertor {

    private SearchRpcConvertor() {
    }

    public static BlogSysSearchReq toSearchReq(BlogSearchQuery query) {
        return BlogSysSearchReq.builder()
            .page(query.currentPage())
            .pageSize(query.pageSize())
            .status(query.selection().status())
            .keywords(query.keywords())
            .createStart(query.selection().createStart())
            .createEnd(query.selection().createEnd())
            .userId(query.selection().userId())
            .allData(query.selection().allData())
            .draftOnly(
                Objects.equals(
                    query.selection().status(),
                    BlogStatusEnum.DRAFT.getCode()))
            .build();
    }

    public static Page<BlogListItem> toBlogPage(AdminBlogSearchRpcVo result) {
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

    public static Page<BlogVersionSummary> toVersionPage(AdminRevisionSearchRpcVo result) {
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

    public static List<BlogReadCountReq> toReadCountRequests(List<BlogReadCount> counts) {
        return counts.stream().map(count -> new BlogReadCountReq(count.blogId(), count.readCount())).toList();
    }

    public static BlogRevisionSearchReq toRevisionSearchReq(
        Long blogId, int page, int pageSize, String keywords, long userId, boolean allData) {
        return new BlogRevisionSearchReq(blogId, page, pageSize, keywords, userId, allData);
    }
}
