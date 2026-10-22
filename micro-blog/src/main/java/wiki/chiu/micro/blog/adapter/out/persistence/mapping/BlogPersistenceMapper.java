package wiki.chiu.micro.blog.adapter.out.persistence.mapping;

import java.util.List;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

/**
 * Translates persistence entities into framework-free projections.
 */
public final class BlogPersistenceMapper {

    private BlogPersistenceMapper() {
    }

    public static BlogIdentity toIdentity(BlogEntity entity) {
        return new BlogIdentity(
            entity.getId(),
            entity.getUserId(),
            entity.getCreated(),
            entity.getReadCount(),
            entity.getPublishedRevisionId(),
            entity.getPublishedAt(),
            entity.getOfflineAt(),
            BlogAccessMode.of(entity.getAccessMode()),
            entity.getPublicationRevision(),
            entity.getAdminRevision());
    }

    public static Blog toPublishedBlog(
        BlogEntity entity, BlogRevisionEntity revision, List<BlogSensitiveSpan> spans) {
        if (entity == null || revision == null) {
            return null;
        }
        return new Blog(
            entity.getId(),
            entity.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            entity.getCreated(),
            revision.getFirstPublishedAt() == null
                ? revision.getCreatedAt()
                : revision.getFirstPublishedAt(),
            status(entity, revision),
            revision.getLink(),
            entity.getReadCount(),
            entity.getPublicationRevision());
    }

    public static Integer status(BlogEntity blog, BlogRevisionEntity revision) {
        if (revision == null) {
            return BlogStatusEnum.DRAFT.getCode();
        }
        if (blog.getOfflineAt() != null
            || BlogAccessMode.of(blog.getAccessMode()) == BlogAccessMode.RESTRICTED) {
            return BlogStatusEnum.HIDE.getCode();
        }
        return revision.getRenderMode().getCode() == BlogStatusEnum.SENSITIVE_FILTER.getCode()
            ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
            : BlogStatusEnum.NORMAL.getCode();
    }

    public static BlogVersion toVersion(
        BlogRevisionEntity entity, List<BlogSensitiveSpan> spans) {
        return new BlogVersion(
            entity.getId(),
            entity.getBlogId(),
            entity.getVersionNo(),
            entity.getOrigin(),
            entity.getBaseRevisionId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getContent(),
            entity.getLink(),
            entity.getRenderMode(),
            entity.getFirstPublishedAt(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            List.copyOf(spans));
    }

    public static BlogWorkingDraft toDraft(
        BlogDraftEntity entity, List<BlogSensitiveSpan> spans) {
        return new BlogWorkingDraft(
            entity.getId(),
            entity.getBlogId(),
            entity.getDraftRevision(),
            entity.getBasePublishedRevisionId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getContent(),
            entity.getLink(),
            entity.getRenderMode(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt(),
            List.copyOf(spans));
    }

    public static BlogSensitiveSpan toSpan(BlogRevisionSensitiveContentEntity entity) {
        return new BlogSensitiveSpan(
            entity.getStartIndex(), entity.getEndIndex(), entity.getType());
    }

    public static BlogSensitiveSpan toSpan(BlogDraftSensitiveContentEntity entity) {
        return new BlogSensitiveSpan(
            entity.getStartIndex(), entity.getEndIndex(), entity.getType());
    }

    public static List<BlogVersion> toVersions(
        List<BlogRevisionEntity> entities,
        List<BlogRevisionSensitiveContentEntity> sensitive) {
        return entities.stream()
            .map(
                entity ->
                    toVersion(
                        entity,
                        sensitive.stream()
                            .filter(span -> span.getRevisionId().equals(entity.getId()))
                            .map(BlogPersistenceMapper::toSpan)
                            .toList()))
            .toList();
    }
}
