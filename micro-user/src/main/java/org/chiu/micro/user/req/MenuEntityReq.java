package org.chiu.micro.user.req;

/**
 * @author mingchiuli
 * @create 2022-12-04 6:23 pm
 */
public record MenuEntityReq(

        Long menuId,

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
