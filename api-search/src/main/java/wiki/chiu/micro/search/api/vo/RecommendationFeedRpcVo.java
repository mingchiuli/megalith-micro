package wiki.chiu.micro.search.api.vo;

import java.util.List;

public record RecommendationFeedRpcVo(
    List<PublicBlogRpcVo> content,
    String nextCursor,
    boolean hasMore,
    boolean restart) {

    public RecommendationFeedRpcVo {
        content = List.copyOf(content);
    }
}
