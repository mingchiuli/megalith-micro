package wiki.chiu.micro.exhibit.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.Page;

/**
 * Renders a use-case page as the page envelope clients receive. This is the boundary where the core
 * page becomes the shared wire shape.
 */
final class PageResponse {

    private PageResponse() {
    }

    static <T> PageAdapter<T> of(Page<?> page, List<T> content) {
        return PageAdapter.<T>builder()
            .content(content)
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }
}
