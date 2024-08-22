package org.chiu.micro.user.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MenuDto implements Serializable {

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
