package wiki.chiu.micro.exhibit.application.model;

import java.util.List;

public record BlogRecommendations(
    List<BlogRecommendation> hot, List<BlogRecommendation> related) {
}
