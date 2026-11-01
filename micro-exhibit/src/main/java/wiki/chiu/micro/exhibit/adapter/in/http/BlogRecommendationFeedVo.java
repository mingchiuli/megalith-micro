package wiki.chiu.micro.exhibit.adapter.in.http;

import java.util.List;

public record BlogRecommendationFeedVo(
    List<BlogRecommendationVo> content,
    String nextCursor,
    boolean hasMore,
    boolean restart) {

    public BlogRecommendationFeedVo {
        content = List.copyOf(content);
    }
}
