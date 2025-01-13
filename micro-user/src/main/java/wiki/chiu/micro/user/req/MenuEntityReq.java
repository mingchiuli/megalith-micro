package wiki.chiu.micro.user.req;

import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-12-04 6:23 pm
 */
public record MenuEntityReq(

        Optional<Long> id,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        String icon,

        Integer orderNum,

        Integer type,

        Integer status) {
}
