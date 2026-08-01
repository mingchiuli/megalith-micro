package wiki.chiu.micro.blog.req;

import java.util.List;
import java.util.Optional;
import wiki.chiu.micro.blog.valid.BlogSaveValue;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
@BlogSaveValue
public record BlogEntityReq(

        Optional<Long> id,

        String title,

        String description,

        String content,

        Integer status,

        String link,

        List<SensitiveContentReq> sensitiveContentList) {
}
