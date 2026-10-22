package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
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
    public Page<Blog> findPage(int pageNumber, int pageSize, List<Integer> statuses) {
        var request =
            PageRequest.of(pageNumber - 1, pageSize, Sort.by("created").descending());
        var page = blogs.findByPublishedRevisionIdIsNotNull(request);
        List<Blog> content =
            page.getContent().stream()
                .map(this::published)
                .flatMap(Optional::stream)
                .filter(blog -> statuses.contains(blog.status()))
                .toList();
        return new Page<>(
            content,
            page.getTotalElements(),
            page.getNumber() + 1,
            page.getSize(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty(),
            page.getTotalPages());
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
