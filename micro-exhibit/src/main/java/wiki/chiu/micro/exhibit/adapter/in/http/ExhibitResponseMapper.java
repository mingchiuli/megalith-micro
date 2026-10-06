package wiki.chiu.micro.exhibit.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.Page;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;

/**
 * Renders the exhibit use-case results as HTTP response bodies.
 */
public final class ExhibitResponseMapper {

    private ExhibitResponseMapper() {
    }

    public static PageAdapter<BlogDescriptionVo> toVo(Page<BlogDescription> page) {
        return PageResponse.of(
            page, page.content().stream().map(ExhibitResponseMapper::toVo).toList());
    }

    public static BlogDescriptionVo toVo(BlogDescription description) {
        return new BlogDescriptionVo(
            description.id(),
            description.title(),
            description.description(),
            description.created(),
            description.link(),
            description.status());
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
}
