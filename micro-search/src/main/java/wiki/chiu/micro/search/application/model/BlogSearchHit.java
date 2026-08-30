package wiki.chiu.micro.search.application.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record BlogSearchHit(
    Long id,
    Long userId,
    Integer status,
    String title,
    String description,
    String content,
    LocalDateTime created,
    Float score,
    Map<String, List<String>> highlight) {}
