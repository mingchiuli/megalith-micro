package wiki.chiu.micro.search.adapter.in.http;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.api.vo.AdminBlogRpcVo;
import wiki.chiu.micro.search.api.vo.AdminBlogSearchRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionRpcVo;
import wiki.chiu.micro.search.api.vo.AdminRevisionSearchRpcVo;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.api.vo.RecommendationFeedRpcVo;
import wiki.chiu.micro.search.api.vo.SearchIndexesRebuildRpcVo;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.RecommendationFeed;
import wiki.chiu.micro.search.application.model.SearchIndexRebuildResult;

public final class SearchResponseConvertor {

    private SearchResponseConvertor() {
    }

    public static PublicBlogRpcVo toResponse(PublicBlogRead blog) {
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

    public static AdminBlogRpcVo toResponse(AdminBlogRead blog) {
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

    public static AdminRevisionRpcVo toResponse(AdminRevisionRead revision) {
        return new AdminRevisionRpcVo(
            revision.id(),
            revision.blogId(),
            revision.versionNo(),
            revision.origin(),
            revision.baseRevisionId(),
            revision.title(),
            revision.firstPublishedAt(),
            revision.createdBy(),
            revision.createdAt(),
            revision.currentPublished());
    }

    public static AdminBlogSearchRpcVo toAdminResponse(Page<AdminBlogRead> page) {
        return new AdminBlogSearchRpcVo(
            page.totalElements(),
            page.pageNumber(),
            page.pageSize(),
            page.content().stream()
                .map(SearchResponseConvertor::toResponse)
                .toList());
    }

    public static AdminRevisionSearchRpcVo toRevisionResponse(Page<AdminRevisionRead> page) {
        return new AdminRevisionSearchRpcVo(
            page.totalElements(),
            page.pageNumber(),
            page.pageSize(),
            page.content().stream()
                .map(SearchResponseConvertor::toResponse)
                .toList());
    }

    public static RecommendationFeedRpcVo toFeedResponse(RecommendationFeed result) {
        return new RecommendationFeedRpcVo(
            result.content().stream()
                .map(SearchResponseConvertor::toResponse)
                .toList(),
            result.nextCursor(),
            result.hasMore(),
            result.restart());
    }

    public static PageAdapter<BlogDocumentVo> toResponse(Page<BlogSearchHit> page) {
        return PageConvertor.of(
            page, page.content().stream().map(SearchResponseConvertor::toResponse).toList());
    }

    public static BlogDocumentVo toResponse(BlogSearchHit hit) {
        return BlogDocumentVo.builder()
            .id(hit.id())
            .userId(hit.userId())
            .status(hit.status())
            .title(hit.title())
            .description(hit.description())
            .content(hit.content())
            .link(hit.link())
            .created(hit.created())
            .score(hit.score())
            .highlight(hit.highlight())
            .build();
    }

    public static SearchIndexesRebuildRpcVo toRebuildResponse(SearchIndexRebuildResult result) {
        return new SearchIndexesRebuildRpcVo(
            result.indexes().stream()
                .map(
                    index ->
                        new IndexRebuildRpcVo(
                            index.previousIndex(),
                            index.index(),
                            index.documents()))
                .toList());
    }
}
