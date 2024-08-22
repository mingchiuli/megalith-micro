package org.chiu.micro.search.dto;

import java.time.LocalDateTime;

import lombok.Data;


@Data
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
