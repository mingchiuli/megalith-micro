package org.chiu.micro.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author mingchiuli
 * @create 2021-12-12 6:55 AM
 */
@Data
@Builder
public class BlogDocumentVo {

    private Long id;

    private Long userId;

    private Integer status;

    private String title;

    private String description;

    private String content;

    private String link;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Float score;

    private Map<String, List<String>> highlight;

}
