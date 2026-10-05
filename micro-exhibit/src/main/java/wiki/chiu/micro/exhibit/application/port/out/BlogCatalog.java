package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;

import wiki.chiu.micro.exhibit.application.model.BlogSummary;

/**
 * Enumerates blog identities in the blog service; no blog content crosses this port.
 */
public interface BlogCatalog {

    List<Long> findIdsAfter(Long afterId, Integer limit);

    List<BlogSummary> findAllById(List<Long> ids);
}
