package org.chiu.micro.exhibit.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BlogExhibitDto implements Serializable {

    private Long userId;

    private String description;

    private String nickname;

    private String avatar;

    private String title;

    private String content;

    private LocalDateTime created;

    private Long readCount;

}
