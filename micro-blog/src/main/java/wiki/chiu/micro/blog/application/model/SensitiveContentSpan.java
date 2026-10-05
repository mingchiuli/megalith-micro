package wiki.chiu.micro.blog.application.model;

/**
 * A marked span of a blog field.
 */
public record SensitiveContentSpan(Integer startIndex, Integer endIndex, Integer type) {
}
