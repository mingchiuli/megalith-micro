package wiki.chiu.micro.blog.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogDraftSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionSensitiveContentEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringStore;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogAccessMode;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.message.BlogAdminIndexMessage;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.model.BlogAdminSnapshot;
import wiki.chiu.micro.common.model.BlogDraftSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class JpaBlogAuthoringAdapter implements BlogAuthoringStore {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;
    private final BlogDraftSensitiveContentRepository draftSensitive;
    private final OutboxEvents outbox;

    public JpaBlogAuthoringAdapter(
        BlogRepository blogs,
        BlogRevisionRepository revisions,
        BlogDraftRepository drafts,
        BlogRevisionSensitiveContentRepository revisionSensitive,
        BlogDraftSensitiveContentRepository draftSensitive,
        OutboxEvents outbox) {
        this.blogs = blogs;
        this.revisions = revisions;
        this.drafts = drafts;
        this.revisionSensitive = revisionSensitive;
        this.draftSensitive = draftSensitive;
        this.outbox = outbox;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlogIdentity> findIdentity(Long blogId) {
        return blogs.findById(blogId).map(BlogPersistenceMapper::toIdentity);
    }

    @Override
    @Transactional
    public BlogIdentity createIdentity(Long userId) {
        BlogEntity entity = blogs.save(BlogEntity.newBlog(userId));
        BlogIdentity identity = BlogPersistenceMapper.toIdentity(entity);
        enqueueAdmin(identity, null, null, "BLOG_CREATED");
        return identity;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlogWorkingDraft> findDraft(Long blogId) {
        return drafts.findByBlogId(blogId)
            .map(
                draft ->
                    BlogPersistenceMapper.toDraft(
                        draft,
                        draftSensitive.findByBlogId(blogId).stream()
                            .map(BlogPersistenceMapper::toSpan)
                            .toList()));
    }

    @Override
    @Transactional
    public BlogWorkingDraft saveDraft(
        Long blogId,
        Long expectedDraftRevision,
        BlogContentCommand content,
        Long userId) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        BlogDraftEntity draft = drafts.findByBlogId(blogId).orElse(null);
        if (expectedDraftRevision != null
            && (draft == null || !Objects.equals(draft.getDraftRevision(), expectedDraftRevision))) {
            throw conflict(blogId);
        }

        LocalDateTime now = LocalDateTime.now();
        if (draft == null) {
            draft =
                BlogDraftEntity.create(
                    blogId,
                    blog.getPublishedRevisionId(),
                    content.title(),
                    content.description(),
                    content.content(),
                    content.link(),
                    content.renderMode(),
                    userId,
                    now);
        } else {
            apply(draft, content, blog.getPublishedRevisionId(), userId, now);
        }
        BlogDraftEntity persisted = drafts.save(draft);
        replaceDraftSensitive(blogId, content.sensitiveContent());
        BlogIdentity identity = bumpAdminRevision(blog);
        BlogWorkingDraft result =
            BlogPersistenceMapper.toDraft(persisted, content.sensitiveContent());
        enqueueAdmin(identity, result, null, "DRAFT_UPSERT");
        return result;
    }

    @Override
    @Transactional
    public void deleteDraft(Long blogId) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        drafts.findByBlogId(blogId).ifPresent(drafts::delete);
        draftSensitive.deleteByBlogId(blogId);
        BlogIdentity identity = bumpAdminRevision(blog);
        enqueueAdmin(identity, null, null, "DRAFT_DELETE");
    }

    @Override
    @Transactional
    public BlogVersion saveDraftVersion(
        Long blogId,
        Long expectedDraftRevision,
        BlogContentCommand content,
        Long userId) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        BlogDraftEntity draft = drafts.findByBlogId(blogId).orElse(null);
        if (expectedDraftRevision != null
            && (draft == null || !Objects.equals(draft.getDraftRevision(), expectedDraftRevision))) {
            throw conflict(blogId);
        }

        LocalDateTime now = LocalDateTime.now();
        if (draft == null) {
            draft =
                BlogDraftEntity.create(
                    blogId,
                    blog.getPublishedRevisionId(),
                    content.title(),
                    content.description(),
                    content.content(),
                    content.link(),
                    content.renderMode(),
                    userId,
                    now);
        } else {
            apply(draft, content, blog.getPublishedRevisionId(), userId, now);
        }
        draft = drafts.save(draft);
        replaceDraftSensitive(blogId, content.sensitiveContent());

        BlogRevisionEntity revision =
            revisions.save(
                new BlogRevisionEntity(
                    null,
                    blogId,
                    nextVersion(blogId),
                    BlogRevisionOrigin.DRAFT,
                    draft.getBasePublishedRevisionId(),
                    draft.getTitle(),
                    draft.getDescription(),
                    draft.getContent(),
                    draft.getLink(),
                    draft.getRenderMode(),
                    null,
                    userId,
                    now));
        replaceRevisionSensitive(
            revision.getId(),
            draftSensitive.findByBlogId(blogId).stream()
                .map(entity -> new BlogSensitiveSpan(
                    entity.getStartIndex(), entity.getEndIndex(), entity.getType()))
                .toList());
        BlogIdentity identity = bumpAdminRevision(blog);
        BlogWorkingDraft workingDraft = BlogPersistenceMapper.toDraft(
            draft,
            draftSensitive.findByBlogId(blogId).stream()
                .map(BlogPersistenceMapper::toSpan)
                .toList());
        BlogVersion version = BlogPersistenceMapper.toVersion(
            revision,
            draftSensitive.findByBlogId(blogId).stream()
                .map(entity -> new BlogSensitiveSpan(
                    entity.getStartIndex(), entity.getEndIndex(), entity.getType()))
                .toList());
        enqueueAdmin(identity, workingDraft, version, "REVISION_CREATE");
        return version;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlogVersion> findVersion(Long blogId, Long revisionId) {
        return revisions.findByIdAndBlogId(revisionId, blogId)
            .map(
                revision ->
                    BlogPersistenceMapper.toVersion(
                        revision,
                        revisionSensitive.findByRevisionId(revisionId).stream()
                            .map(BlogPersistenceMapper::toSpan)
                            .toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlogVersion> findPreviousVersion(Long blogId, Long revisionId) {
        BlogRevisionEntity current = revisions.findByIdAndBlogId(revisionId, blogId).orElse(null);
        if (current == null) {
            return Optional.empty();
        }
        return revisions.findFirstByBlogIdAndVersionNoLessThanOrderByVersionNoDesc(
                blogId, current.getVersionNo())
            .map(
                revision ->
                    BlogPersistenceMapper.toVersion(
                        revision,
                        revisionSensitive.findByRevisionId(revision.getId()).stream()
                            .map(BlogPersistenceMapper::toSpan)
                            .toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogVersion> findVersions(Long blogId) {
        List<BlogRevisionEntity> entities = revisions.findByBlogIdOrderByVersionNoAsc(blogId);
        return BlogPersistenceMapper.toVersions(
            entities,
            revisionSensitive.findByRevisionIdIn(
                entities.stream().map(BlogRevisionEntity::getId).toList()));
    }

    @Override
    @Transactional
    public void publish(Long blogId, Long revisionId, Long userId, Integer accessMode) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        BlogRevisionEntity revision =
            revisions.findByIdAndBlogId(revisionId, blogId)
                .orElseThrow(() -> conflict(blogId));
        Long latestVersion =
            revisions.findFirstByBlogIdOrderByVersionNoDesc(blogId)
                .map(BlogRevisionEntity::getVersionNo)
                .orElse(null);
        if (revision.getFirstPublishedAt() == null
            && revision.getOrigin() == BlogRevisionOrigin.DRAFT
            && !revision.getVersionNo().equals(latestVersion)) {
            throw conflict(blogId);
        }
        LocalDateTime now = LocalDateTime.now();
        if (revision.getFirstPublishedAt() == null) {
            revision.setFirstPublishedAt(now);
        }
        blog.setPublishedRevisionId(revisionId);
        blog.setPublishedAt(now);
        blog.setOfflineAt(null);
        blog.setAccessMode(BlogAccessMode.of(accessMode).getCode());
        blog.setPublicationRevision(blog.getPublicationRevision() + 1);
        blog.setAdminRevision(blog.getAdminRevision() + 1);

        BlogVersion archivedDraft = archiveDraftIfNeeded(blog, revision, userId, now);
        BlogIdentity identity = BlogPersistenceMapper.toIdentity(blog);
        BlogVersion version = version(revision);
        BlogSnapshot published = publishedSnapshot(blog, revision);
        enqueuePublic(BlogOperateEnum.UPDATE.getCode(), published, userId);
        enqueueAdmin(identity, currentDraft(blogId), version, "PUBLICATION_UPSERT");
        if (archivedDraft != null) {
            enqueueAdmin(identity, null, archivedDraft, "REVISION_CREATE");
        }
    }

    @Override
    @Transactional
    public void offline(Long blogId, Long userId) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        if (blog.getPublishedRevisionId() == null) {
            throw conflict(blogId);
        }
        blog.setOfflineAt(LocalDateTime.now());
        blog.setPublicationRevision(blog.getPublicationRevision() + 1);
        blog.setAdminRevision(blog.getAdminRevision() + 1);
        BlogRevisionEntity revision = requireRevision(blog);
        enqueuePublic(
            BlogOperateEnum.UPDATE.getCode(), publishedSnapshot(blog, revision), userId);
        enqueueAdmin(
            BlogPersistenceMapper.toIdentity(blog),
            currentDraft(blogId),
            version(revision),
            "OFFLINE");
    }

    @Override
    @Transactional
    public void online(Long blogId, Long userId) {
        BlogEntity blog = requireBlogForUpdate(blogId);
        if (blog.getPublishedRevisionId() == null) {
            throw conflict(blogId);
        }
        blog.setOfflineAt(null);
        blog.setPublishedAt(LocalDateTime.now());
        blog.setPublicationRevision(blog.getPublicationRevision() + 1);
        blog.setAdminRevision(blog.getAdminRevision() + 1);
        BlogRevisionEntity revision = requireRevision(blog);
        enqueuePublic(
            BlogOperateEnum.UPDATE.getCode(), publishedSnapshot(blog, revision), userId);
        enqueueAdmin(
            BlogPersistenceMapper.toIdentity(blog),
            currentDraft(blogId),
            version(revision),
            "ONLINE");
    }

    @Override
    @Transactional
    public void rebuildProjections(Long blogId) {
        BlogEntity blog =
            blogs.findById(blogId)
                .orElseThrow(
                    () ->
                        new BaseException(
                            CommonErrorCode.CONFLICT, "blog not found: " + blogId));
        BlogIdentity identity = BlogPersistenceMapper.toIdentity(blog);
        BlogWorkingDraft draft = currentDraft(blogId);
        BlogRevisionEntity revision =
            blog.getPublishedRevisionId() == null
                ? null
                : revisions.findById(blog.getPublishedRevisionId()).orElse(null);
        BlogVersion version = revision == null ? null : version(revision);
        enqueueAdmin(identity, draft, version, "REBUILD");
        if (revision != null) {
            enqueuePublic(
                BlogOperateEnum.UPDATE.getCode(),
                publishedSnapshot(blog, revision),
                blog.getUserId());
        }
    }

    private BlogVersion archiveDraftIfNeeded(
        BlogEntity blog, BlogRevisionEntity published, Long userId, LocalDateTime now) {
        BlogDraftEntity draft = drafts.findByBlogId(blog.getId()).orElse(null);
        if (draft == null) {
            return null;
        }
        if (same(draft, published)) {
            drafts.delete(draft);
            draftSensitive.deleteByBlogId(blog.getId());
            return null;
        }
        BlogRevisionEntity archived =
            revisions.save(
                new BlogRevisionEntity(
                    null,
                    blog.getId(),
                    nextVersion(blog.getId()),
                    BlogRevisionOrigin.DRAFT,
                    draft.getBasePublishedRevisionId(),
                    draft.getTitle(),
                    draft.getDescription(),
                    draft.getContent(),
                    draft.getLink(),
                    draft.getRenderMode(),
                    null,
                    userId,
                    now));
        replaceRevisionSensitive(
            archived.getId(),
            draftSensitive.findByBlogId(blog.getId()).stream()
                .map(entity -> new BlogSensitiveSpan(
                    entity.getStartIndex(), entity.getEndIndex(), entity.getType()))
                .toList());
        drafts.delete(draft);
        draftSensitive.deleteByBlogId(blog.getId());
        return version(archived);
    }

    private boolean same(BlogDraftEntity draft, BlogRevisionEntity revision) {
        return Objects.equals(draft.getTitle(), revision.getTitle())
            && Objects.equals(draft.getDescription(), revision.getDescription())
            && Objects.equals(draft.getContent(), revision.getContent())
            && Objects.equals(draft.getLink(), revision.getLink())
            && draft.getRenderMode() == revision.getRenderMode();
    }

    private void apply(
        BlogDraftEntity draft,
        BlogContentCommand content,
        Long publishedRevisionId,
        Long userId,
        LocalDateTime now) {
        draft.setBasePublishedRevisionId(publishedRevisionId);
        draft.setTitle(content.title());
        draft.setDescription(content.description());
        draft.setContent(content.content());
        draft.setLink(content.link());
        draft.setRenderMode(content.renderMode());
        draft.setUpdatedBy(userId);
        draft.setUpdatedAt(now);
        draft.setDraftRevision(draft.getDraftRevision() + 1);
    }

    private void replaceDraftSensitive(Long blogId, List<BlogSensitiveSpan> spans) {
        draftSensitive.deleteByBlogId(blogId);
        draftSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        new BlogDraftSensitiveContentEntity(
                            null,
                            blogId,
                            span.startIndex(),
                            span.endIndex(),
                            span.type()))
                .toList());
    }

    private void replaceRevisionSensitive(Long revisionId, List<BlogSensitiveSpan> spans) {
        revisionSensitive.deleteByRevisionId(revisionId);
        revisionSensitive.saveAll(
            spans.stream()
                .distinct()
                .map(
                    span ->
                        new BlogRevisionSensitiveContentEntity(
                            null,
                            revisionId,
                            span.startIndex(),
                            span.endIndex(),
                            span.type()))
                .toList());
    }

    private BlogEntity requireBlogForUpdate(Long blogId) {
        return blogs.findForUpdate(blogId)
            .orElseThrow(() -> new BaseException(CommonErrorCode.CONFLICT, "blog not found"));
    }

    private BlogRevisionEntity requireRevision(BlogEntity blog) {
        return revisions.findById(blog.getPublishedRevisionId())
            .orElseThrow(() -> conflict(blog.getId()));
    }

    private long nextVersion(Long blogId) {
        return revisions.findFirstByBlogIdOrderByVersionNoDesc(blogId)
            .map(revision -> revision.getVersionNo() + 1)
            .orElse(1L);
    }

    private BlogIdentity bumpAdminRevision(BlogEntity blog) {
        blog.setAdminRevision(blog.getAdminRevision() + 1);
        return BlogPersistenceMapper.toIdentity(blog);
    }

    private BlogVersion version(BlogRevisionEntity revision) {
        return BlogPersistenceMapper.toVersion(
            revision,
            revisionSensitive.findByRevisionId(revision.getId()).stream()
                .map(BlogPersistenceMapper::toSpan)
                .toList());
    }

    private BlogWorkingDraft currentDraft(Long blogId) {
        return findDraft(blogId).orElse(null);
    }

    private BlogSnapshot publishedSnapshot(BlogEntity blog, BlogRevisionEntity revision) {
        List<BlogSensitiveSpan> spans =
            revisionSensitive.findByRevisionId(revision.getId()).stream()
                .map(BlogPersistenceMapper::toSpan)
                .toList();
        Integer status = BlogPersistenceMapper.status(blog, revision);
        return new BlogSnapshot(
            blog.getId(),
            blog.getUserId(),
            revision.getTitle(),
            revision.getDescription(),
            revision.getContent(),
            blog.getCreated(),
            revision.getFirstPublishedAt(),
            status,
            revision.getLink(),
            blog.getReadCount(),
            blog.getPublicationRevision(),
            revision.getId(),
            blog.getPublishedAt(),
            blog.getOfflineAt(),
            blog.getAccessMode(),
            revision.getRenderMode().getCode(),
            spans);
    }

    private BlogAdminSnapshot adminSnapshot(BlogIdentity identity) {
        return new BlogAdminSnapshot(
            identity.id(),
            identity.userId(),
            identity.publishedRevisionId(),
            identity.publishedAt(),
            identity.offlineAt(),
            identity.accessMode().getCode(),
            identity.publicationRevision(),
            identity.adminRevision(),
            identity.readCount(),
            identity.created());
    }

    private BlogDraftSnapshot draftSnapshot(BlogWorkingDraft draft) {
        if (draft == null) {
            return null;
        }
        return new BlogDraftSnapshot(
            draft.blogId(),
            draft.draftRevision(),
            draft.basePublishedRevisionId(),
            draft.title(),
            draft.description(),
            draft.content(),
            draft.link(),
            draft.renderMode(),
            draft.updatedBy(),
            draft.updatedAt(),
            draft.sensitiveContent());
    }

    private BlogRevisionSnapshot revisionSnapshot(BlogVersion version) {
        if (version == null) {
            return null;
        }
        return new BlogRevisionSnapshot(
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

    private void enqueueAdmin(
        BlogIdentity identity,
        BlogWorkingDraft draft,
        BlogVersion revision,
        String operation) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            identity.id(),
            eventId ->
                new BlogAdminIndexMessage(
                    eventId,
                    operation,
                    adminSnapshot(identity),
                    draftSnapshot(draft),
                    revisionSnapshot(revision)));
    }

    private void enqueuePublic(Integer operation, BlogSnapshot snapshot, Long userId) {
        outbox.enqueue(
            OutboxProducer.BLOG,
            "BLOG",
            snapshot.id(),
            eventId ->
                new BlogChangedMessage(
                    eventId, operation, snapshot.revision(), userId, snapshot));
    }

    private BaseException conflict(Long blogId) {
        return new BaseException(CommonErrorCode.CONFLICT, "blog revision conflict: " + blogId);
    }
}
