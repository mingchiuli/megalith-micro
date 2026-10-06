package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogSummary;
import wiki.chiu.micro.exhibit.application.model.Page;

/**
 * Turns the blog service payloads into the exhibit service's own model.
 */
public final class BlogHttpMapper {

    private BlogHttpMapper() {
    }

    public static Page<BlogDescription> toDescriptions(PageAdapter<BlogEntityRpcVo> page) {
        List<BlogDescription> content =
            page.content().stream().map(BlogHttpMapper::toDescription).toList();
        return new Page<>(
            content,
            page.totalElements(),
            page.pageNumber(),
            page.pageSize(),
            page.first(),
            page.last(),
            page.empty(),
            page.totalPages());
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
