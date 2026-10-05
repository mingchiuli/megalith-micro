package wiki.chiu.micro.exhibit.adapter.in.http;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-03-19 3:27 am
 */
public record BlogExhibitVo(
    String description,
    String nickname,
    String avatar,
    String title,
    String content,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
    Long readCount) {
}
