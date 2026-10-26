package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.BlogSummary;
import wiki.chiu.micro.exhibit.application.model.Page;

/**
 * Turns the blog service payloads into the exhibit service's own model.
 */
public final class BlogHttpMapper {

    private BlogHttpMapper() {
    }

    public static List<BlogSummary> toSummaries(List<BlogEntityRpcVo> blogs) {
        return blogs.stream()
            .map(blog -> new BlogSummary(blog.id(), blog.title(), blog.status()))
            .toList();
    }

    public static Page<BlogRecommendation> toRecommendationPage(
        PageAdapter<PublicBlogRpcVo> page) {
        return new Page<>(
            page.content().stream()
                .map(BlogHttpMapper::toRecommendation)
                .toList(),
            page.totalElements(),
            page.pageNumber(),
            page.pageSize(),
            page.first(),
            page.last(),
            page.empty(),
            page.totalPages());
    }

    public static BlogRecommendation toRecommendation(PublicBlogRpcVo blog) {
        return new BlogRecommendation(
            blog.id(),
            blog.title(),
            blog.description(),
            blog.publishedAt() == null ? blog.created() : blog.publishedAt(),
            blog.link(),
            blog.readCount());
    }

    public static BlogHotRead toHotRead(PublicBlogRpcVo blog) {
        return new BlogHotRead(blog.id(), blog.title(), blog.readCount());
    }

    public static int publicStatus(PublicBlogRpcVo blog) {
        if (blog.offlineAt() != null || Integer.valueOf(1).equals(blog.accessMode())) {
            return BlogStatusEnum.HIDE.getCode();
        }
        return blog.renderMode() == BlogRenderMode.SENSITIVE_FILTER
            ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
            : BlogStatusEnum.NORMAL.getCode();
    }
}
