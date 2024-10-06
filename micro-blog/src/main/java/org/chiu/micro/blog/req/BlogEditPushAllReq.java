package org.chiu.micro.blog.req;

import java.util.List;
import java.util.Optional;

public record BlogEditPushAllReq(

        Optional<Long> id,

        String title,

        String description,

        String content,

        Integer status,

        String link,

        Integer version,

        List<SensitiveContentReq> sensitiveContentList) {
}
