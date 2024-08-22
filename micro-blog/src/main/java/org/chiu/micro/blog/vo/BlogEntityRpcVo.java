package org.chiu.micro.blog.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogEntityRpcVo {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private Long readCount;

    private String content;

    private String link;

    private LocalDateTime created;

    private LocalDateTime updated;

    private Integer status;
}
