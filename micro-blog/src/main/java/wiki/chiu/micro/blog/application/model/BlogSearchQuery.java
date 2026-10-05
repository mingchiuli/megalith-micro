package wiki.chiu.micro.blog.application.model;

/**
 * One page of blog ids to look up in the search index. The keyword reaches the index, while the
 * selection carries the filters the index cannot apply on its own.
 */
public record BlogSearchQuery(
    int currentPage, int pageSize, String keywords, BlogSearchSelection selection) {
}
