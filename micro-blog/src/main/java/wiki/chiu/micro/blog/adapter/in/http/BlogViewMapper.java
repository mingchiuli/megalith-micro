package wiki.chiu.micro.blog.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.common.page.PageAdapter;

/**
 * Renders the blog use-case results as HTTP response bodies.
 */
public final class BlogViewMapper {

    private BlogViewMapper() {
    }

    public static PageAdapter<BlogEntityVo> toVo(PageAdapter<BlogListItem> page) {
        return PageAdapter.<BlogEntityVo>builder()
            .content(page.content().stream().map(BlogViewMapper::toVo).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    public static BlogEntityVo toVo(BlogListItem blog) {
        return BlogEntityVo.builder()
            .id(blog.id())
            .title(blog.title())
            .description(blog.description())
            .readCount(blog.readCount())
            .recentReadCount(blog.recentReadCount())
            .status(blog.status())
            .link(blog.link())
            .created(blog.created())
            .updated(blog.updated())
            .content(blog.content())
            .build();
    }

    public static PageAdapter<BlogDeleteVo> toDeletedVo(PageAdapter<DeletedBlogItem> page) {
        return PageAdapter.<BlogDeleteVo>builder()
            .content(page.content().stream().map(BlogViewMapper::toDeletedVo).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    public static BlogDeleteVo toDeletedVo(DeletedBlogItem blog) {
        return BlogDeleteVo.builder()
            .idx(blog.idx())
            .link(blog.link())
            .content(blog.content())
            .readCount(blog.readCount())
            .title(blog.title())
            .status(blog.status())
            .created(blog.created())
            .updated(blog.updated())
            .id(blog.id())
            .userId(blog.userId())
            .description(blog.description())
            .build();
    }

    public static BlogEditVo toVo(BlogEdit blog) {
        List<BlogEditVo.SensitiveContentVo> spans =
            blog.sensitiveContentList().stream()
                .map(
                    span ->
                        BlogEditVo.SensitiveContentVo.builder()
                            .type(span.type())
                            .startIndex(span.startIndex())
                            .endIndex(span.endIndex())
                            .build())
                .toList();
        return BlogEditVo.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .link(blog.link())
            .status(blog.status())
            .permissions(
                new BlogPermissionsVo(
                    blog.permissions().collaborate(),
                    blog.permissions().commit(),
                    blog.permissions().manageMetadata(),
                    blog.permissions().manageAssets()))
            .sensitiveContentList(spans)
            .build();
    }
}
