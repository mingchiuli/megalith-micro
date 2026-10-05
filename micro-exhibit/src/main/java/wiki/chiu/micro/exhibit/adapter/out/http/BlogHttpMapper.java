package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogSummary;

/**
 * Turns the blog service payloads into the exhibit service's own model.
 */
public final class BlogHttpMapper {

    private BlogHttpMapper() {
    }

    public static PageAdapter<BlogDescription> toDescriptions(PageAdapter<BlogEntityRpcVo> page) {
        List<BlogDescription> content =
            page.content().stream().map(BlogHttpMapper::toDescription).toList();
        return PageAdapter.<BlogDescription>builder()
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

    public static BlogDescription toDescription(BlogEntityRpcVo blog) {
        return new BlogDescription(
            blog.id(), blog.title(), blog.description(), blog.status(), blog.created(), blog.link());
    }

    public static List<BlogSummary> toSummaries(List<BlogEntityRpcVo> blogs) {
        return blogs.stream()
            .map(blog -> new BlogSummary(blog.id(), blog.title(), blog.status()))
            .toList();
    }
}
