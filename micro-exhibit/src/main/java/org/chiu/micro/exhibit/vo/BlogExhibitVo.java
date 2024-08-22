package org.chiu.micro.exhibit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2023-03-19 3:27 am
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogExhibitVo {

    private String description;

    private String nickname;

    private String avatar;

    private String title;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Long readCount;
}
