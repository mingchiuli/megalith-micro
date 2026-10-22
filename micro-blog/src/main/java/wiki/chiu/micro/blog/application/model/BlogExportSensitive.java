package wiki.chiu.micro.blog.application.model;

public record BlogExportSensitive(
    Long revisionId, Integer startIndex, Integer endIndex, Integer type) {
}
