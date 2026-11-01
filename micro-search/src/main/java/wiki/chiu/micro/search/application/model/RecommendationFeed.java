package wiki.chiu.micro.search.application.model;

import java.util.List;

public record RecommendationFeed(
    List<PublicBlogRead> content,
    String nextCursor,
    boolean hasMore,
    boolean restart) {

    public RecommendationFeed {
        content = List.copyOf(content);
    }
}
