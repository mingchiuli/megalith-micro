package wiki.chiu.micro.exhibit.adapter.in.http;

import java.util.List;

public record BlogRecommendationsVo(
    List<BlogRecommendationVo> hot, List<BlogRecommendationVo> related) {
}
