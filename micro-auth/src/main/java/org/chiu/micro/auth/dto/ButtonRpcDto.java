package org.chiu.micro.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
@Builder
@Data
public class ButtonRpcDto {

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
