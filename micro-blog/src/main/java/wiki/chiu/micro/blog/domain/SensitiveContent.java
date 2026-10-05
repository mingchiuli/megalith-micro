package wiki.chiu.micro.blog.domain;

import java.time.LocalDateTime;

/**
 * A span of a blog the author marked as sensitive. The field order matches the published wire
 * order: the indices come first, then the field the span belongs to.
 */
public record SensitiveContent(
    Long id,
    Long blogId,
    Integer startIndex,
    Integer endIndex,
    Integer type,
    LocalDateTime created,
    LocalDateTime updated) {

    public SensitiveContent onBlog(Long newBlogId) {
        return new SensitiveContent(id, newBlogId, startIndex, endIndex, type, created, updated);
    }
}
