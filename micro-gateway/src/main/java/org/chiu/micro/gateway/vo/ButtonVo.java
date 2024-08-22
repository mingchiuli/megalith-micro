package org.chiu.micro.gateway.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
@Data
@Builder
public class ButtonVo {

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
