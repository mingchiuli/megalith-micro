package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.convertor.BlogPersistenceConvertor;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
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
                            BlogPersistenceConvertor::toSpan, Collectors.toList())));
        return entities.stream()
            .map(
                blog -> {
                    BlogRevisionEntity revision =
                        revisionsById.get(blog.getPublishedRevisionId());
                    if (revision == null) {
                        return null;
                    }
                    return BlogPersistenceConvertor.snapshot(
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
                                BlogPersistenceConvertor::toSpan, Collectors.toList())));
        Map<Long, BlogDraftEntity> draftsByBlog =
            drafts.findByBlogIdIn(blogIds).stream()
                .collect(Collectors.toMap(BlogDraftEntity::getBlogId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByDraft =
            draftSensitive.findByBlogIdIn(blogIds).stream()
                .collect(
                    Collectors.groupingBy(
                        span -> span.getBlogId(),
                        Collectors.mapping(
                            BlogPersistenceConvertor::toSpan, Collectors.toList())));

        return entities.stream()
            .map(
                blog -> {
                    BlogRevisionEntity revision =
                        revisionsById.get(blog.getPublishedRevisionId());
                    BlogRevisionSnapshot publishedRevision =
                        BlogPersistenceConvertor.revisionSnapshot(
                            revision,
                            revision == null
                                ? List.of()
                                : spansByRevision.getOrDefault(revision.getId(), List.of()));
                    BlogDraftEntity draft = draftsByBlog.get(blog.getId());
                    return BlogPersistenceConvertor.toAdminIndexSnapshot(blog, draft, spansByDraft.getOrDefault(blog.getId(), List.of()), publishedRevision);
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
                            BlogPersistenceConvertor::toSpan, Collectors.toList())));
        return entities.stream()
            .map(
                revision ->
                    BlogPersistenceConvertor.revisionSnapshot(
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
        return blogIds.isEmpty() ? List.of() : deletionTargets(blogs.findAllById(blogIds));
    }

    @Override
    public List<BlogDeletionTarget> findDeletionTargetsByUserIds(List<Long> userIds) {
        return userIds.isEmpty() ? List.of() : deletionTargets(blogs.findByUserIdIn(userIds));
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
                    BlogPersistenceConvertor.toPublishedBlog(
                        blog, revision, spans(revision.getId())));
    }

    private List<BlogDeletionTarget> deletionTargets(List<BlogEntity> entities) {
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
            revisionIds.isEmpty()
                ? Map.of()
                : revisions.findAllById(revisionIds).stream()
                    .collect(Collectors.toMap(BlogRevisionEntity::getId, Function.identity()));
        Map<Long, List<BlogSensitiveSpan>> spansByRevision =
            revisionIds.isEmpty()
                ? Map.of()
                : revisionSensitive.findByRevisionIdIn(revisionIds).stream()
                    .collect(Collectors.groupingBy(
                        span -> span.getRevisionId(),
                        Collectors.mapping(BlogPersistenceConvertor::toSpan, Collectors.toList())));
        List<Long> draftIds =
            entities.stream()
                .filter(blog -> blog.getPublishedRevisionId() == null
                    || !revisionsById.containsKey(blog.getPublishedRevisionId()))
                .map(BlogEntity::getId)
                .toList();
        Map<Long, BlogDraftEntity> draftsByBlog =
            draftIds.isEmpty()
                ? Map.of()
                : drafts.findByBlogIdIn(draftIds).stream()
                    .collect(Collectors.toMap(BlogDraftEntity::getBlogId, Function.identity()));
        return entities.stream()
            .map(entity -> {
                BlogRevisionEntity revision = entity.getPublishedRevisionId() == null
                    ? null : revisionsById.get(entity.getPublishedRevisionId());
                BlogDraftEntity draft = draftsByBlog.get(entity.getId());
                return BlogPersistenceConvertor.toDeletionTarget(
                    entity, revision, draft,
                    revision == null ? List.of() : spansByRevision.getOrDefault(revision.getId(), List.of()));
            })
            .toList();
    }

    private List<BlogSensitiveSpan> spans(Long revisionId) {
        return revisionSensitive.findByRevisionId(revisionId).stream()
            .map(BlogPersistenceConvertor::toSpan)
            .toList();
    }
}
