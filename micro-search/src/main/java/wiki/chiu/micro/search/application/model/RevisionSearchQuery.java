package wiki.chiu.micro.search.application.model;

public record RevisionSearchQuery(
    long blogId,
    int page,
    int pageSize,
    String keywords,
    long userId,
    boolean allData) {
}
