package org.chiu.micro.blog.req;

import lombok.Data;
import java.util.List;


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
