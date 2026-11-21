package wiki.chiu.micro.exhibit.adapter.out.http.search;

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
