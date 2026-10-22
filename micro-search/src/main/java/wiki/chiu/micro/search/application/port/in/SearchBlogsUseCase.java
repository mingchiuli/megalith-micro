package wiki.chiu.micro.search.application.port.in;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RecommendationResult;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;

public interface SearchBlogsUseCase {

    Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query);

    Page<PublicBlogRead> publicPage(int page, int pageSize);

    PublicBlogRead findPublic(long blogId);

    RecommendationResult recommendations(Long blogId, int limit);

    Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query);

    Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query);

    BlogSearchResult searchPrivate(PrivateBlogSearchQuery query);

    long countPrivate(PrivateBlogSearchQuery query);

    void updateReadCounts(List<BlogReadCount> counts);
}
