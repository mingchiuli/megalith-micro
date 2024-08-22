package org.chiu.micro.gateway.vo;

import lombok.Builder;
import lombok.Data;


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

    private String created;

    private String updated;

    private Integer status;
}
