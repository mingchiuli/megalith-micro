package org.chiu.micro.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuEntityVo {

    private Long menuId;

    private Long parentId;

    private String title;

    private String name;

    private String url;

    private String component;

    private Integer type;

    private String icon;

    private Integer orderNum;

    private Integer status;
}
