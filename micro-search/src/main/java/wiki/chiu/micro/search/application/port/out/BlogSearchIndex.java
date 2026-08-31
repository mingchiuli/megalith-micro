package wiki.chiu.micro.search.application.port.out;

import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;

public interface BlogSearchIndex {

    SearchPage<BlogSearchHit> searchPublic(PublicBlogSearchQuery query);

    BlogSearchResult searchPrivate(PrivateBlogSearchQuery query);

    long countPrivate(PrivateBlogSearchQuery query);

    void incrementViews(long blogId);
}
