package wiki.chiu.micro.exhibit.adapter.out.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;
import wiki.chiu.micro.search.api.vo.PublicBlogRpcVo;

class BlogHttpConvertorTest {

    @Test
    void masksSensitiveTitleAndDescriptionOnRelatedCards() {
        PublicBlogRpcVo blog =
            new PublicBlogRpcVo(
                7L,
                "safe secret",
                "public secret",
                "https://cdn.example/7.png",
                10L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                BlogRenderMode.SENSITIVE_FILTER,
                List.of(
                    new BlogSensitiveSpan(5, 11, 1),
                    new BlogSensitiveSpan(7, 13, 2)));

        var recommendation = BlogHttpConvertor.toRecommendation(blog);

        assertThat(recommendation.title()).isEqualTo("safe ++++++");
        assertThat(recommendation.description()).isEqualTo("public ++++++");
    }
}
