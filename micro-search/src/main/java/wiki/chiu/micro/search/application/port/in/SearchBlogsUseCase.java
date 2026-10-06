package wiki.chiu.micro.search.application.port.in;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;

public interface SearchBlogsUseCase {

    Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query);

    BlogSearchResult searchPrivate(PrivateBlogSearchQuery query);

    long countPrivate(PrivateBlogSearchQuery query);

    void updateReadCounts(List<BlogReadCount> counts);
}
