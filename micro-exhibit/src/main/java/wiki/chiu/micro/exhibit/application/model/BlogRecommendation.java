package wiki.chiu.micro.exhibit.application.model;

import java.time.LocalDateTime;

public record BlogRecommendation(
    Long id,
    String title,
    String description,
    LocalDateTime created,
    String link,
    Long readCount) {
}
