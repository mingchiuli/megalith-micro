package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.entity.BlogRevisionEntity;
import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogDraftSensitiveContentRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionSensitiveContentRepository;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

@Component
public class JpaBlogAuthoringReader implements BlogAuthoringReader {

    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final BlogDraftRepository drafts;
    private final BlogDraftSensitiveContentRepository draftSensitive;
    private final BlogRevisionSensitiveContentRepository revisionSensitive;

    public JpaBlogAuthoringReader(
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
    public Optional<BlogIdentity> findIdentity(Long blogId) {
        return blogs.findById(blogId).map(BlogPersistenceMapper::toIdentity);
    }

    @Override
    public Optional<BlogWorkingDraft> findDraft(Long blogId) {
        return drafts
            .findByBlogId(blogId)
            .map(
                draft ->
                    BlogPersistenceMapper.toDraft(
                        draft,
                        draftSensitive.findByBlogId(blogId).stream()
                            .map(BlogPersistenceMapper::toSpan)
                            .toList()));
    }

    @Override
    public Optional<BlogVersion> findVersion(Long blogId, Long revisionId) {
        return revisions
            .findByIdAndBlogId(revisionId, blogId)
            .map(this::version);
    }

    @Override
    public Optional<BlogVersion> findPreviousVersion(Long blogId, Long revisionId) {
        return revisions
            .findByIdAndBlogId(revisionId, blogId)
            .flatMap(
                current ->
                    revisions.findFirstByBlogIdAndVersionNoLessThanOrderByVersionNoDesc(
                        blogId, current.getVersionNo()))
            .map(this::version);
    }

    @Override
    public Optional<BlogVersion> findPublishedVersion(Long blogId) {
        return blogs
            .findById(blogId)
            .map(entity -> entity.getPublishedRevisionId())
            .flatMap(revisions::findById)
            .map(this::version);
    }

    @Override
    public Optional<BlogVersion> findLatestVersion(Long blogId) {
        return revisions.findFirstByBlogIdOrderByVersionNoDesc(blogId).map(this::version);
    }

    @Override
    public List<BlogVersion> findVersions(Long blogId) {
        List<BlogRevisionEntity> entities = revisions.findByBlogIdOrderByVersionNoAsc(blogId);
        return BlogPersistenceMapper.toVersions(
            entities,
            entities.isEmpty()
                ? List.of()
                : revisionSensitive.findByRevisionIdIn(
                    entities.stream().map(BlogRevisionEntity::getId).toList()));
    }

    private BlogVersion version(BlogRevisionEntity revision) {
        return BlogPersistenceMapper.toVersion(
            revision,
            revisionSensitive.findByRevisionId(revision.getId()).stream()
                .map(BlogPersistenceMapper::toSpan)
                .toList());
    }
}
