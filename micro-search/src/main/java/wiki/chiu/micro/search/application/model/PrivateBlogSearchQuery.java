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
    boolean allData,
    boolean draftOnly) {

    public PrivateBlogSearchQuery(
        int page,
        int pageSize,
        String keywords,
        Integer status,
        LocalDateTime createStart,
        LocalDateTime createEnd,
        long userId,
        boolean allData) {
        this(
            page,
            pageSize,
            keywords,
            status,
            createStart,
            createEnd,
            userId,
            allData,
            false);
    }
}
