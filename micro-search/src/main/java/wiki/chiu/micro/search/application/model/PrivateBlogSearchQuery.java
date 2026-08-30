package wiki.chiu.micro.search.application.model;

import java.time.LocalDateTime;

public record PrivateBlogSearchQuery(
    int page,
    int pageSize,
    String keywords,
    Integer status,
    LocalDateTime createStart,
    LocalDateTime createEnd,
    long userId,
    boolean allData) {}
