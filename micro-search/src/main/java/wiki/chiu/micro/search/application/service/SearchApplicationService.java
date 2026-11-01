package wiki.chiu.micro.search.application.service;

import java.util.List;

import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.RecommendationFeed;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;

public final class SearchApplicationService implements SearchBlogsUseCase {

    private final BlogSearchIndex searchIndex;
    private final BlogIndexWriter indexWriter;

    public SearchApplicationService(
        BlogSearchIndex searchIndex, BlogIndexWriter indexWriter) {
        this.searchIndex = searchIndex;
        this.indexWriter = indexWriter;
    }

    @Override
    public Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        return searchIndex.searchPublic(query);
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
    public RecommendationFeed recommendationFeed(RecommendationFeedQuery query) {
        try {
            return searchIndex.recommendationFeed(query);
        } catch (IllegalArgumentException failure) {
            if ("invalid recommendation cursor".equals(failure.getMessage())) {
                throw new ValidationException(failure.getMessage());
            }
            throw failure;
        }
    }

    @Override
    public List<PublicBlogRead> recommendationHead(int limit) {
        return searchIndex.recommendationHead(limit);
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
    public void updateReadCounts(List<BlogReadCount> counts) {
        indexWriter.updateReadCounts(counts);
    }
}
