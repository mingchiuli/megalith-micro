package wiki.chiu.micro.exhibit.application.model;

/**
 * The identity of a blog as known by the blog service, used to compose the hot-read ranking.
 */
public record BlogSummary(Long id, String title, Integer status) {
}
