package wiki.chiu.micro.blog.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.blog.api.vo.SensitiveContentRpcVo;
import wiki.chiu.micro.blog.application.model.BlogSensitiveSpans;
import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;

/**
 * Renders the blog use-case results as the published RPC payloads.
 */
public final class BlogRpcMapper {

    private BlogRpcMapper() {
    }

    public static BlogEntityRpcVo toRpc(BlogSnapshot blog) {
        return BlogEntityRpcVo.builder()
            .id(blog.id())
            .title(blog.title())
            .link(blog.link())
            .readCount(blog.readCount())
            .description(blog.description())
            .content(blog.content())
            .userId(blog.userId())
            .created(blog.created())
            .updated(blog.updated())
            .status(blog.status())
            .build();
    }

    public static PageAdapter<BlogEntityRpcVo> toRpc(PageAdapter<BlogSnapshot> page) {
        return PageAdapter.<BlogEntityRpcVo>builder()
            .content(page.content().stream().map(BlogRpcMapper::toRpc).toList())
            .totalElements(page.totalElements())
            .pageNumber(page.pageNumber())
            .pageSize(page.pageSize())
            .first(page.first())
            .last(page.last())
            .empty(page.empty())
            .totalPages(page.totalPages())
            .build();
    }

    public static List<BlogEntityRpcVo> toRpc(List<BlogSnapshot> blogs) {
        return blogs.stream().map(BlogRpcMapper::toRpc).toList();
    }

    public static BlogSensitiveContentRpcVo toRpc(BlogSensitiveSpans spans) {
        return BlogSensitiveContentRpcVo.builder()
            .blogId(spans.blogId())
            .sensitiveContent(
                spans.sensitiveContent().stream()
                    .map(
                        span ->
                            SensitiveContentRpcVo.builder()
                                .type(span.type())
                                .startIndex(span.startIndex())
                                .endIndex(span.endIndex())
                                .build())
                    .toList())
            .build();
    }

    public static BlogIndexSourceStatus toRpc(IndexSourceStatus status) {
        return new BlogIndexSourceStatus(
            status.readOnly(), status.readyEvents(), status.pausedEvents(), status.total());
    }
}
