package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;

@Component
public class JpaBlogQueryAdapter implements BlogQueryStore {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;

    public JpaBlogQueryAdapter(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogRevisionSensitiveContentRepository revisionSensitive) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.revisionSensitive = revisionSensitive;
    }

    @Override
    public List<Long> findIdsAfter(Long afterId, int limit) {
        return blogs.findIdsAfter(afterId, PageRequest.of(0, limit));
    }

    @Override
    public List<BlogReadCount> findReadCountsAfter(long afterId, int limit) {
        return blogs.findReadCountsAfter(afterId, PageRequest.of(0, limit));
    }

    @Override
    public List<BlogSnapshot> findPublishedSnapshotsAfter(long afterId, int limit) {
        return blogs
            .findByPublishedRevisionIdIsNotNullAndIdGreaterThanOrderByIdAsc(
                afterId, PageRequest.of(0, limit))
            .stream()
            .map(this::snapshot)
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public Optional<Blog> findById(Long blogId) {
        return blogs.findById(blogId).flatMap(this::published);
    }

    @Override
    public List<BlogDeletionTarget> findDeletionTargetsByIds(List<Long> blogIds) {
        return blogs.findAllById(blogIds).stream()
            .map(this::deletionTarget)
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public List<BlogDeletionTarget> findDeletionTargetsByUserIds(List<Long> userIds) {
        return blogs.findByUserIdIn(userIds).stream()
            .map(this::deletionTarget)
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public long count() {
        return blogs.countByPublishedRevisionIdIsNotNull();
    }

    @Override
    public List<BlogSensitiveSpan> findSensitiveByBlogId(Long blogId) {
        return blogs.findById(blogId)
            .map(BlogEntity::getPublishedRevisionId)
            .flatMap(revisions::findById)
            .map(
                revision ->
                    revisionSensitive.findByRevisionId(revision.getId()).stream()
                        .map(BlogPersistenceMapper::toSpan)
                        .toList())
            .orElseGet(List::of);
    }

    private Optional<Blog> published(BlogEntity blog) {
        if (blog.getPublishedRevisionId() == null) {
            return Optional.empty();
        }
        return revisions.findById(blog.getPublishedRevisionId())
            .map(
                revision ->
                    BlogPersistenceMapper.toPublishedBlog(
                        blog, revision, spans(revision.getId())));
    }

    private Optional<BlogSnapshot> snapshot(BlogEntity blog) {
        if (blog.getPublishedRevisionId() == null) {
            return Optional.empty();
        }
        return revisions.findById(blog.getPublishedRevisionId())
            .map(
                revision -> {
                    List<BlogSensitiveSpan> sensitive = spans(revision.getId());
                    return new BlogSnapshot(
                        blog.getId(),
                        blog.getUserId(),
                        revision.getTitle(),
                        revision.getDescription(),
                        revision.getContent(),
                        blog.getCreated(),
                        revision.getFirstPublishedAt(),
                        BlogPersistenceMapper.status(blog, revision),
                        revision.getLink(),
                        blog.getReadCount(),
                        blog.getPublicationRevision(),
                        revision.getId(),
                        blog.getPublishedAt(),
                        blog.getOfflineAt(),
                        blog.getAccessMode(),
                        revision.getRenderMode().getCode(),
                        sensitive);
                });
    }

    private Optional<Blog> publishedOrDraft(BlogEntity blog) {
        Optional<Blog> published = published(blog);
        if (published.isPresent()) {
            return published;
        }
        return drafts.findByBlogId(blog.getId())
            .map(draft -> draftProjection(blog, draft));
    }

    private Optional<BlogDeletionTarget> deletionTarget(BlogEntity entity) {
        Blog blog =
            publishedOrDraft(entity)
                .orElseGet(
                    () ->
                        new Blog(
                            entity.getId(),
                            entity.getUserId(),
                            "",
                            "",
                            "",
                            entity.getCreated(),
                            entity.getCreated(),
                            BlogStatusEnum.DRAFT.getCode(),
                            "",
                            entity.getReadCount(),
                            entity.getAdminRevision()));
        BlogIdentity identity = BlogPersistenceMapper.toIdentity(entity);
        return Optional.of(
            new BlogDeletionTarget(
                blog,
                new BlogIdentity(
                    identity.id(),
                    identity.userId(),
                    identity.created(),
                    identity.readCount(),
                    identity.publishedRevisionId(),
                    identity.publishedAt(),
                    identity.offlineAt(),
                    identity.accessMode(),
                    identity.publicationRevision() == null
                        ? 0L
                        : identity.publicationRevision(),
                    identity.adminRevision() == null ? 0L : identity.adminRevision())));
    }

    private Blog draftProjection(BlogEntity blog, BlogDraftEntity draft) {
        return new Blog(
            blog.getId(),
            blog.getUserId(),
            draft.getTitle(),
            draft.getDescription(),
            draft.getContent(),
            blog.getCreated(),
            draft.getUpdatedAt(),
            BlogStatusEnum.DRAFT.getCode(),
            draft.getLink(),
            blog.getReadCount(),
            blog.getAdminRevision());
    }

    private List<BlogSensitiveSpan> spans(Long revisionId) {
        return revisionSensitive.findByRevisionId(revisionId).stream()
            .map(BlogPersistenceMapper::toSpan)
            .toList();
    }
}
