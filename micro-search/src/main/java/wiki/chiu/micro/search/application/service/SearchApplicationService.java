package wiki.chiu.micro.search.application.service;

import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.SearchPage;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

public final class SearchApplicationService implements SearchBlogsUseCase {

  private final BlogSearchIndex searchIndex;

  public SearchApplicationService(BlogSearchIndex searchIndex) {
    this.searchIndex = searchIndex;
  }

  @Override
  public SearchPage<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
    return searchIndex.searchPublic(query);
  }

  @Override
  public BlogSearchResult searchPrivate(PrivateBlogSearchQuery query) {
    return searchIndex.searchPrivate(query);
  }

  @Override
  public long countPrivate(PrivateBlogSearchQuery query) {
    return searchIndex.countPrivate(query);
  }

  @Override
  public void incrementViews(long blogId) {
    searchIndex.incrementViews(blogId);
  }
}
