package org.chiu.micro.gateway.vo;

import lombok.Data;

/**
 * @author mingchiuli
 * @create 2023-04-12 1:05 pm
 */
@Data
public class BlogDescriptionVo {

    private Long id;

    private String title;

    private String description;

    private String created;

    private String link;
}