package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public interface BlogAuthoringStore {

    Optional<BlogIdentity> findIdentity(Long blogId);

    BlogIdentity createIdentity(Long userId);

    Optional<BlogWorkingDraft> findDraft(Long blogId);

    BlogWorkingDraft saveDraft(
        Long blogId,
        Long expectedDraftRevision,
        BlogContentCommand content,
        Long userId);

    void deleteDraft(Long blogId);

    BlogVersion saveDraftVersion(
        Long blogId,
        Long expectedDraftRevision,
        BlogContentCommand content,
        Long userId);

    Optional<BlogVersion> findVersion(Long blogId, Long revisionId);

    Optional<BlogVersion> findPreviousVersion(Long blogId, Long revisionId);

    List<BlogVersion> findVersions(Long blogId);

    void publish(Long blogId, Long revisionId, Long userId, Integer accessMode);

    void offline(Long blogId, Long userId);

    void online(Long blogId, Long userId);

    void rebuildProjections(Long blogId);
}
