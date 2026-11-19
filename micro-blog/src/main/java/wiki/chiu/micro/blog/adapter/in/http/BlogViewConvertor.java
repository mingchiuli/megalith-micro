package wiki.chiu.micro.blog.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogVersionComparison;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.SavedBlogVersion;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.common.page.PageAdapter;

/**
 * Renders the blog use-case results as HTTP response bodies.
 */
public final class BlogViewConvertor {

    private BlogViewConvertor() {
    }

    public static PageAdapter<BlogEntityVo> toVo(Page<BlogListItem> page) {
        return PageConvertor.of(page, page.content().stream().map(BlogViewConvertor::toVo).toList());
    }

    public static BlogEntityVo toVo(BlogListItem blog) {
        return BlogEntityVo.builder()
            .id(blog.id())
            .title(blog.title())
            .description(blog.description())
            .readCount(blog.readCount())
            .recentReadCount(blog.recentReadCount())
            .status(blog.status())
            .link(blog.link())
            .created(blog.created())
            .updated(blog.updated())
            .content(blog.content())
            .hasDraft(blog.hasDraft())
            .draftNewer(blog.draftNewer())
            .build();
    }

    public static PageAdapter<BlogDeleteVo> toDeletedVo(Page<DeletedBlogItem> page) {
        return PageConvertor.of(
            page, page.content().stream().map(BlogViewConvertor::toDeletedVo).toList());
    }

    public static BlogDeleteVo toDeletedVo(DeletedBlogItem blog) {
        return BlogDeleteVo.builder()
            .recoveryId(blog.recoveryId())
            .link(blog.link())
            .content(blog.content())
            .readCount(blog.readCount())
            .title(blog.title())
            .status(blog.status())
            .created(blog.created())
            .updated(blog.updated())
            .id(blog.id())
            .userId(blog.userId())
            .description(blog.description())
            .build();
    }

    public static BlogEditVo toVo(BlogEdit blog) {
        List<BlogEditVo.SensitiveContentVo> spans =
            blog.sensitiveContentList().stream()
                .map(
                    span ->
                        BlogEditVo.SensitiveContentVo.builder()
                            .type(span.type())
                            .startIndex(span.startIndex())
                            .endIndex(span.endIndex())
                            .build())
                .toList();
        return BlogEditVo.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .link(blog.link())
            .status(blog.status())
            .collaborationEpoch(blog.collaborationEpoch())
            .draftRevision(blog.draftRevision())
            .basePublishedRevisionId(blog.basePublishedRevisionId())
            .publishedRevisionId(blog.publishedRevisionId())
            .hasDraft(blog.hasDraft())
            .draftNewer(blog.draftNewer())
            .permissions(
                new BlogPermissionsVo(
                    blog.permissions().collaborate(),
                    blog.permissions().commit(),
                    blog.permissions().manageMetadata(),
                    blog.permissions().manageAssets()))
            .sensitiveContentList(spans)
            .build();
    }

    public static BlogVersionVo toVo(BlogVersion version) {
        return new BlogVersionVo(
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
            version.sensitiveContent());
    }

    public static BlogVersionSaveVo toSaveVo(SavedBlogVersion saved) {
        return new BlogVersionSaveVo(
            saved.draft().draftRevision(),
            toVo(saved.version()));
    }

    public static BlogVersionComparisonVo toVo(BlogVersionComparison comparison) {
        return new BlogVersionComparisonVo(
            toVo(comparison.previous()), toVo(comparison.current()));
    }

    public static BlogIdVo createdBlog(BlogIdentity identity) {
        return new BlogIdVo(identity.id(), identity.collaborationEpoch());
    }

    public static PageAdapter<BlogVersionSummaryVo> toVersionSummaries(
        Page<BlogVersionSummary> page) {
        return PageConvertor.of(
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
                            version.firstPublishedAt(),
                            version.createdBy(),
                            version.createdAt(),
                            version.currentPublished()))
                .toList());
    }

    public static BlogPublicationRpcVo toPublicationRpc(BlogIdentity identity) {
        return new BlogPublicationRpcVo(identity.publishedRevisionId(), identity.userId(),
            identity.accessMode(), identity.created(), identity.publishedAt(), identity.readCount());
    }

    public static BlogPublicationContentRpcVo toPublicationContentRpc(BlogVersion version) {
        return new BlogPublicationContentRpcVo(version.title(), version.description(),
            version.content(), version.renderMode(), version.sensitiveContent());
    }

    public static BlogIndexSourceStatus toIndexSourceStatus(IndexSourceStatus status) {
        return new BlogIndexSourceStatus(status.readOnly(), status.readyEvents(), status.pausedEvents(),
            status.total(), status.publishedTotal());
    }
}
