package wiki.chiu.micro.search.application.port.out;

import java.util.List;
import java.util.Optional;

import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;

public interface BlogSearchIndex {

    Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query);

    Optional<PublicBlogRead> findPublic(long blogId);

    List<PublicBlogRead> relatedArticles(long blogId, int limit);

    Page<PublicBlogRead> recommendationPage(int page, int pageSize);

    Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query);

    Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query);

    void updateReadCounts(List<BlogReadCount> counts);
}
