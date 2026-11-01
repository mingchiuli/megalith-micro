package wiki.chiu.micro.exhibit.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;

/**
 * Renders the exhibit use-case results as HTTP response bodies.
 */
public final class ExhibitResponseMapper {

    private ExhibitResponseMapper() {
    }

    public static BlogExhibitVo toVo(BlogExhibit exhibit) {
        return new BlogExhibitVo(
            exhibit.description(),
            exhibit.nickname(),
            exhibit.avatar(),
            exhibit.title(),
            exhibit.content(),
            exhibit.created(),
            exhibit.readCount());
    }

    public static VisitStatisticsVo toVo(VisitStatistics statistics) {
        return new VisitStatisticsVo(
            statistics.dayVisit(),
            statistics.weekVisit(),
            statistics.monthVisit(),
            statistics.yearVisit());
    }

    public static List<BlogHotReadVo> toHotReadVos(List<BlogHotRead> hotReads) {
        return hotReads.stream()
            .map(hotRead -> new BlogHotReadVo(hotRead.id(), hotRead.title(), hotRead.readCount()))
            .toList();
    }

    public static BlogRecommendationFeedVo toRecommendationFeedVo(
        RecommendationFeed feed) {
        return new BlogRecommendationFeedVo(
            feed.content().stream().map(ExhibitResponseMapper::toVo).toList(),
            feed.nextCursor(),
            feed.hasMore(),
            feed.restart());
    }

    private static BlogRecommendationVo toVo(BlogRecommendation recommendation) {
        return new BlogRecommendationVo(
            recommendation.id(),
            recommendation.title(),
            recommendation.description(),
            recommendation.created(),
            recommendation.link(),
            recommendation.readCount());
    }
}
