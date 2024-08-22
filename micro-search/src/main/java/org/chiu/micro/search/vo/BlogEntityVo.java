package org.chiu.micro.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-12-03 11:36 pm
 */
@Builder
@Data
public class BlogEntityVo {

    private Long id;

    private String title;

    private Boolean owner;

    private String description;

    private String content;

    private String link;

    private Long readCount;

    private Integer recentReadCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    private Integer status;
}
