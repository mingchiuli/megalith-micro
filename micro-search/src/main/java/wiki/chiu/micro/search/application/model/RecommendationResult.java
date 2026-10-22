package wiki.chiu.micro.search.application.model;

import java.util.List;

public record RecommendationResult(List<PublicBlogRead> hot, List<PublicBlogRead> related) {
}
