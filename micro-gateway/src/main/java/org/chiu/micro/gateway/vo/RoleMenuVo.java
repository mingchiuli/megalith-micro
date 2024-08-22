package org.chiu.micro.gateway.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleMenuVo {

    private Long menuId;

    private String title;

    //是否选了
    private Boolean check;

    private List<RoleMenuVo> children = new ArrayList<>();
}
