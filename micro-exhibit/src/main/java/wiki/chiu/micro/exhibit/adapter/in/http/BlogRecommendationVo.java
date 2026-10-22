package wiki.chiu.micro.exhibit.adapter.in.http;

import java.time.LocalDateTime;

public record BlogRecommendationVo(
    Long id,
    String title,
    String description,
    LocalDateTime created,
    String link,
    Long readCount) {
}
