package wiki.chiu.micro.search.application.port.in;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;

public interface SearchBlogsUseCase {

    Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query);

    PublicBlogRead findPublic(long blogId);

    Page<PublicBlogRead> recommendationPage(int page, int pageSize);

    Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query);

    Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query);

    void updateReadCounts(List<BlogReadCount> counts);
}
