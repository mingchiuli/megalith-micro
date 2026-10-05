package wiki.chiu.micro.exhibit.adapter.in.http;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-04-12 1:05 pm
 */
public record BlogDescriptionVo(
    Long id,
    String title,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
    String link,
    Integer status) {
}
