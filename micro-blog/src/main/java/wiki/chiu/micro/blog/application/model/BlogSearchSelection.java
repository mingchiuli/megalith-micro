package wiki.chiu.micro.blog.application.model;

import java.time.LocalDateTime;
import java.util.Objects;

import wiki.chiu.micro.blog.domain.BlogEntity;

public record BlogSearchSelection(
    Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, boolean allData) {

    public boolean includes(BlogEntity blog) {
        return (allData || Objects.equals(userId, blog.getUserId()))
            && (status == null || Objects.equals(status, blog.getStatus()))
            && (createStart == null || !blog.getCreated().isBefore(createStart))
            && (createEnd == null || !blog.getCreated().isAfter(createEnd));
    }
}
