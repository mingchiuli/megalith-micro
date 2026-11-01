package wiki.chiu.micro.blog.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;

public interface BlogAuthoringReader {

    Optional<BlogIdentity> findIdentity(Long blogId);

    Optional<BlogWorkingDraft> findDraft(Long blogId);

    Optional<BlogVersion> findVersion(Long blogId, Long revisionId);

    Optional<BlogVersion> findPreviousVersion(Long blogId, Long revisionId);

    Optional<BlogVersion> findPublishedVersion(Long blogId);

    Optional<BlogVersion> findLatestVersion(Long blogId);

    List<BlogVersion> findVersions(Long blogId);
}
