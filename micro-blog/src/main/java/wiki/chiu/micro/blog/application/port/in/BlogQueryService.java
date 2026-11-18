package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;

public interface BlogQueryService {

    BlogIdentity publication(Long blogId);

    BlogVersion publicationContent(Long blogId, Long revisionId);

    List<Long> findIdsAfter(Long afterId, Integer limit);

    void incrementViews(Long blogId);
}
