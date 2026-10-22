package wiki.chiu.micro.search.application.service;

import java.util.List;

import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.RecommendationResult;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

public final class SearchApplicationService implements SearchBlogsUseCase {

    private final BlogSearchIndex searchIndex;

    public SearchApplicationService(BlogSearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }

    @Override
    public Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        return searchIndex.searchPublic(query);
    }

    @Override
    public Page<PublicBlogRead> publicPage(int page, int pageSize) {
        return searchIndex.publicPage(page, pageSize);
    }

    @Override
    public PublicBlogRead findPublic(long blogId) {
        PublicBlogRead blog = searchIndex.findPublic(blogId)
            .orElseThrow(
                () ->
                    new MissException(ExceptionMessage.NO_FOUND.getMsg()));
        if (blog.offlineAt() != null) {
            throw new MissException(ExceptionMessage.NO_FOUND.getMsg());
        }
        return blog;
    }

    @Override
    public RecommendationResult recommendations(Long blogId, int limit) {
        return searchIndex.recommendations(blogId, limit);
    }

    @Override
    public Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query) {
        return searchIndex.searchAdmin(query);
    }

    @Override
    public Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query) {
        return searchIndex.searchRevisions(query);
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
    public void updateReadCounts(List<BlogReadCount> counts) {
        searchIndex.updateReadCounts(counts);
    }
}
