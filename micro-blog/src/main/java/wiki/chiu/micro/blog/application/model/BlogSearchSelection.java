package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;
import java.util.Objects;

import wiki.chiu.micro.blog.domain.Blog;

/**
 * The filters the search index cannot apply on its own, re-applied to the rows it returned.
 */
public record BlogSearchSelection(
    Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, boolean allData) {

    /**
     * Re-applies the filters the search index cannot apply on its own.
     */
    public boolean includes(Blog blog) {
        return (allData || Objects.equals(userId, blog.userId()))
            && (status == null || Objects.equals(status, blog.status()))
            && (createStart == null || !blog.created().isBefore(createStart))
            && (createEnd == null || !blog.created().isAfter(createEnd));
    }
}
