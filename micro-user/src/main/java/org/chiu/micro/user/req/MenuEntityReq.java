package org.chiu.micro.user.req;

import lombok.Data;

/**
 * @author mingchiuli
 * @create 2022-12-04 6:23 pm
 */
@Data
public class MenuEntityReq {

    private Long menuId;

    private Long parentId;

    private String title;

    private String name;

    private String url;

    private String component;

    private String icon;

    private Integer orderNum;

    private Integer type;

    private Integer status;
}
