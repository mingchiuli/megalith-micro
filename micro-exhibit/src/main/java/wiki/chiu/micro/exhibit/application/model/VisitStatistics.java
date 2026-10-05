package wiki.chiu.micro.exhibit.application.model;

/**
 * The daily, weekly, monthly, and yearly visit counters.
 */
public record VisitStatistics(Long dayVisit, Long weekVisit, Long monthVisit, Long yearVisit) {
}
