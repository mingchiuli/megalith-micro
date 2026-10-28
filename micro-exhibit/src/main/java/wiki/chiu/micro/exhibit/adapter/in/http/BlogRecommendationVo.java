package wiki.chiu.micro.exhibit.adapter.in.http;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record BlogRecommendationVo(
    Long id,
    String title,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
    String link,
    Long readCount) {
}
