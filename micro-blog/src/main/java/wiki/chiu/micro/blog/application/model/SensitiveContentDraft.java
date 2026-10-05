package wiki.chiu.micro.blog.application.model;

/**
 * A sensitive span as submitted by the author.
 */
public record SensitiveContentDraft(Integer type, Integer startIndex, Integer endIndex) {
}
