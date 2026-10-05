package wiki.chiu.micro.blog.application.model;

import java.util.List;

/**
 * The page of blog ids the search index matched.
 */
public record BlogSearchResult(long total, int currentPage, int pageSize, List<Long> ids) {
}
