package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
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
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;

@Component
public class JpaBlogQueryAdapter implements BlogQueryStore {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogDraftSensitiveContentRepository draftSensitive;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;

    public JpaBlogQueryAdapter(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogDraftSensitiveContentRepository draftSensitive,
        BlogRevisionSensitiveContentRepository revisionSensitive) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.draftSensitive = draftSensitive;
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
        List<BlogEntity> entities =
            blogs
            .findByPublishedRevisionIdIsNotNullAndIdGreaterThanOrderByIdAsc(
                afterId, PageRequest.of(0, limit));
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> revisionIds =
            entities.stream()
                .map(BlogEntity::getPublishedRevisionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, BlogRevisionEntity> revisionsById =
            revisions.findAllById(revisionIds).stream()
                .collect(Collectors.toMap(BlogRevisionEntity::getId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByRevision =
            revisionSensitive.findByRevisionIdIn(revisionIds).stream()
                .collect(
                    Collectors.groupingBy(
                        span -> span.getRevisionId(),
                        Collectors.mapping(
                            BlogPersistenceMapper::toSpan, Collectors.toList())));
        return entities.stream()
            .map(
                blog -> {
                    BlogRevisionEntity revision =
                        revisionsById.get(blog.getPublishedRevisionId());
                    if (revision == null) {
                        return null;
                    }
                    return snapshot(
                        blog,
                        revision,
                        spansByRevision.getOrDefault(revision.getId(), List.of()));
                })
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public List<BlogAdminIndexSnapshot> findAdminSnapshotsAfter(long afterId, int limit) {
        List<BlogEntity> entities =
            blogs.findByIdGreaterThanOrderByIdAsc(afterId, PageRequest.of(0, limit));
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> blogIds = entities.stream().map(BlogEntity::getId).toList();
        List<Long> revisionIds =
            entities.stream()
                .map(BlogEntity::getPublishedRevisionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, BlogRevisionEntity> revisionsById =
            revisionIds.isEmpty()
                ? Map.of()
                : revisions.findAllById(revisionIds).stream()
                    .collect(Collectors.toMap(BlogRevisionEntity::getId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByRevision =
            revisionIds.isEmpty()
                ? Map.of()
                : revisionSensitive.findByRevisionIdIn(revisionIds).stream()
                    .collect(
                        Collectors.groupingBy(
                            span -> span.getRevisionId(),
                            Collectors.mapping(
                                BlogPersistenceMapper::toSpan, Collectors.toList())));
        Map<Long, BlogDraftEntity> draftsByBlog =
            drafts.findByBlogIdIn(blogIds).stream()
                .collect(Collectors.toMap(BlogDraftEntity::getBlogId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByDraft =
            draftSensitive.findByBlogIdIn(blogIds).stream()
                .collect(
                    Collectors.groupingBy(
                        span -> span.getBlogId(),
                        Collectors.mapping(
                            BlogPersistenceMapper::toSpan, Collectors.toList())));

        return entities.stream()
            .map(
                blog -> {
                    BlogRevisionEntity revision =
                        revisionsById.get(blog.getPublishedRevisionId());
                    BlogRevisionSnapshot publishedRevision =
                        revisionSnapshot(
                            revision,
                            revision == null
                                ? List.of()
                                : spansByRevision.getOrDefault(revision.getId(), List.of()));
                    BlogDraftEntity draft = draftsByBlog.get(blog.getId());
                    return new BlogAdminIndexSnapshot(
                        adminSnapshot(blog),
                        draftSnapshot(
                            draft,
                            spansByDraft.getOrDefault(blog.getId(), List.of())),
                        publishedRevision,
                        publishedRevision);
                })
            .toList();
    }

    @Override
    public List<BlogRevisionSnapshot> findRevisionSnapshotsAfter(
        long afterRevisionId, int limit) {
        List<BlogRevisionEntity> entities =
            revisions.findByIdGreaterThanOrderByIdAsc(
                afterRevisionId, PageRequest.of(0, limit));
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> revisionIds = entities.stream().map(BlogRevisionEntity::getId).toList();
        Map<Long, List<BlogSensitiveSpan>> spansByRevision =
            revisionSensitive.findByRevisionIdIn(revisionIds).stream()
                .collect(
                    Collectors.groupingBy(
                        span -> span.getRevisionId(),
                        Collectors.mapping(
                            BlogPersistenceMapper::toSpan, Collectors.toList())));
        return entities.stream()
            .map(
                revision ->
                    revisionSnapshot(
                        revision,
                        spansByRevision.getOrDefault(revision.getId(), List.of())))
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

    private static BlogSnapshot snapshot(
        BlogEntity blog,
        BlogRevisionEntity revision,
        List<BlogSensitiveSpan> sensitive) {
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
    }

    private static BlogAdminSnapshot adminSnapshot(BlogEntity blog) {
        return new BlogAdminSnapshot(
            blog.getId(),
            blog.getUserId(),
            blog.getPublishedRevisionId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            blog.getPublicationRevision(),
            blog.getAdminRevision(),
            blog.getReadCount(),
            blog.getCreated());
    }

    private static BlogDraftSnapshot draftSnapshot(
        BlogDraftEntity draft, List<BlogSensitiveSpan> spans) {
        if (draft == null) {
            return null;
        }
        return new BlogDraftSnapshot(
            draft.getBlogId(),
            draft.getDraftRevision(),
            draft.getBasePublishedRevisionId(),
            draft.getTitle(),
            draft.getDescription(),
            draft.getContent(),
            draft.getLink(),
            draft.getRenderMode(),
            draft.getUpdatedBy(),
            draft.getUpdatedAt(),
            spans);
    }

    private static BlogRevisionSnapshot revisionSnapshot(
        BlogRevisionEntity revision, List<BlogSensitiveSpan> spans) {
        if (revision == null) {
            return null;
        }
        return new BlogRevisionSnapshot(
            revision.getId(),
            revision.getBlogId(),
            revision.getVersionNo(),
            revision.getOrigin(),
            revision.getBaseRevisionId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            revision.getLink(),
            revision.getRenderMode(),
            revision.getFirstPublishedAt(),
            revision.getCreatedBy(),
            revision.getCreatedAt(),
            spans);
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
