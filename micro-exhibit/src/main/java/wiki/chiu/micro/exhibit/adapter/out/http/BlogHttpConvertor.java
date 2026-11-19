package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.SensitiveTypeEnum;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;
import wiki.chiu.micro.exhibit.domain.SensitiveContentMasker;
import wiki.chiu.micro.exhibit.domain.SensitiveSpan;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;
import wiki.chiu.micro.search.api.vo.RecommendationFeedRpcVo;

/**
 * Turns search service payloads into the exhibit service's own model.
 */
public final class BlogHttpConvertor {

    private BlogHttpConvertor() {
    }

    public static RecommendationFeed toRecommendationFeed(
        RecommendationFeedRpcVo feed) {
        return new RecommendationFeed(
            feed.content().stream()
                .map(BlogHttpConvertor::toRecommendation)
                .toList(),
            feed.nextCursor(),
            feed.hasMore(),
            feed.restart());
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
}
