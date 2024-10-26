package wiki.chiu.micro.blog.req;

import java.util.List;
import java.util.Optional;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
public record BlogEntityReq(

        Optional<Long> id,

        String title,

        String description,

        String content,

        Integer status,

        String link,

        List<SensitiveContentReq> sensitiveContentList) {
}
