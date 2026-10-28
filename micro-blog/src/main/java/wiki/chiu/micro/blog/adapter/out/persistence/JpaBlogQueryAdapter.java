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
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
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
            revisions.findAllById(revisionIds).stream()
                .collect(Collectors.toMap(BlogRevisionEntity::getId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByRevision =
            revisionIds.isEmpty()
                ? Map.of()
                : revisionSensitive.findByRevisionIdIn(revisionIds).stream()
                    .collect(
                        Collectors.groupingBy(
                            span -> span.getRevisionId(),
                            Collectors.mapping(BlogPersistenceMapper::toSpan, Collectors.toList())));
        Map<Long, BlogDraftEntity> draftsByBlog =
            drafts.findByBlogIdIn(blogIds).stream()
                .collect(Collectors.toMap(BlogDraftEntity::getBlogId, Function.identity()));

        return entities.stream()
            .map(
                blog -> {
                    BlogRevisionEntity revision =
                        revisionsById.get(blog.getPublishedRevisionId());
                    BlogDraftEntity draft = draftsByBlog.get(blog.getId());
                    return new BlogAdminIndexSnapshot(
                        adminSnapshot(blog),
                        draftSnapshot(draft),
                        revisionSnapshot(
                            revision,
                            revision == null
                                ? List.of()
                                : spansByRevision.getOrDefault(revision.getId(), List.of())));
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
                        Collectors.mapping(BlogPersistenceMapper::toSpan, Collectors.toList())));
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
    public List<Blog> findAllById(List<Long> blogIds) {
        return blogs.findAllById(blogIds).stream()
            .map(this::published)
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public List<Blog> findByUserIds(List<Long> userIds) {
        return blogs.findByUserIdIn(userIds).stream()
            .map(this::publishedOrDraft)
            .flatMap(Optional::stream)
            .toList();
    }

    @Override
    public long count() {
        return blogs.countByPublishedRevisionIdIsNotNull();
    }

    @Override
    public List<SensitiveContent> findSensitiveByBlogId(Long blogId) {
        return blogs.findById(blogId)
            .map(BlogEntity::getPublishedRevisionId)
            .flatMap(revisions::findById)
            .map(
                revision ->
                    revisionSensitive.findByRevisionId(revision.getId()).stream()
                        .map(
                            span ->
                                new SensitiveContent(
                                    span.getId(),
                                    blogId,
                                    span.getStartIndex(),
                                    span.getEndIndex(),
                                    span.getType(),
                                    null,
                                    null))
                        .toList())
            .orElseGet(List::of);
    }

    @Override
    public List<SensitiveContent> findSensitiveByBlogIds(List<Long> blogIds) {
        return blogIds.stream()
            .flatMap(blogId -> findSensitiveByBlogId(blogId).stream())
            .toList();
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

    private static BlogDraftSnapshot draftSnapshot(BlogDraftEntity draft) {
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
            List.of());
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
