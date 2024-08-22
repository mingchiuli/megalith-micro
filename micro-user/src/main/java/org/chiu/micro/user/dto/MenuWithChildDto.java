package org.chiu.micro.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MenuWithChildDto {

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
    private List<MenuWithChildDto> children = new ArrayList<>();
}
