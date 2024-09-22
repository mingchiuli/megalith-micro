package org.chiu.micro.blog.req;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
public record BlogEntityReq(

        Long id,

        String title,

        String description,

        String content,

        Integer status,

        String link,

        List<SensitiveContentReq> sensitiveContentList) {
}
