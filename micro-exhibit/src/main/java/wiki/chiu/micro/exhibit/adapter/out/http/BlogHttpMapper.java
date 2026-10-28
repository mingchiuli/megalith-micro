package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.SensitiveTypeEnum;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.Page;
import wiki.chiu.micro.exhibit.domain.SensitiveContentMasker;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

/**
 * Turns the blog service payloads into the exhibit service's own model.
 */
public final class BlogHttpMapper {

    private BlogHttpMapper() {
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
        List<SensitiveSpan> spans =
            blog.renderMode() == BlogRenderMode.SENSITIVE_FILTER
                ? blog.sensitiveContent().stream()
                    .flatMap(
                        span ->
                            SensitiveTypeEnum.ofCode(span.type()).stream()
                                .map(
                                    type ->
                                        new SensitiveSpan(
                                            type, span.startIndex(), span.endIndex())))
                    .toList()
                : List.of();
        return new BlogRecommendation(
            blog.id(),
            mask(blog.title(), spans, SensitiveTypeEnum.TITLE),
            mask(blog.description(), spans, SensitiveTypeEnum.DESCRIPTION),
            blog.publishedAt() == null ? blog.created() : blog.publishedAt(),
            blog.link(),
            blog.readCount());
    }

    private static String mask(
        String content, List<SensitiveSpan> spans, SensitiveTypeEnum type) {
        return content == null
            ? ""
            : SensitiveContentMasker.mask(content, spans, type);
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
