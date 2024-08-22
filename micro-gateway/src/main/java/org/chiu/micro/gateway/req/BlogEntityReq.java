package org.chiu.micro.gateway.req;


import java.util.List;

import lombok.Data;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
@Data
public class BlogEntityReq {

    private Long id;

    private String title;

    private String description;

    private String content;

    private Integer status;

    private String link;

    private List<SensitiveContentReq> sensitiveContentList;
}
