package wiki.chiu.micro.exhibit.application.model;

import java.util.List;

public record RecommendationFeed(
    List<BlogRecommendation> content,
    String nextCursor,
    boolean hasMore,
    boolean restart) {

    public RecommendationFeed {
        content = List.copyOf(content);
    }
}
