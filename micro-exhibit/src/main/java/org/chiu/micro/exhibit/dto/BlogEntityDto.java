package org.chiu.micro.exhibit.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BlogEntityDto {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String content;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;

    private String link;

    private Long readCount;
}
