package org.chiu.micro.blog.req;

import java.util.List;

public record BlogEditPushAllReq(

        Long id,

        String title,

        String description,

        String content,

        Integer status,

        String link,

        Integer version,

        List<SensitiveContentReq> sensitiveContentList) {
}
