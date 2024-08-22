package org.chiu.micro.gateway.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDisplayVo {

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

    private String created;

    private String updated;

    private List<MenuDisplayVo> children = new ArrayList<>();
}
