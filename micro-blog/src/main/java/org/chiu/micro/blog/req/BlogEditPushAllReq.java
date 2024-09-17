package org.chiu.micro.blog.req;

import java.util.List;

import lombok.Data;

@Data
public class BlogEditPushAllReq {

    private Long id;

    private String title;

    private String description;

    private String content;

    private Integer status;

    private String link;

    private Integer version;

    private List<SensitiveContentReq> sensitiveContentList;
    
}
