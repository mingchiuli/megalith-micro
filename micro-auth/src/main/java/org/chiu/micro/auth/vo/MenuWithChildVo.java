package org.chiu.micro.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MenuWithChildVo {

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

    @Builder.Default
    private List<MenuWithChildVo> children = new ArrayList<>();
}
