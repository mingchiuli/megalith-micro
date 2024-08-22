package org.chiu.micro.user.vo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class RoleMenuVo {

    private Long menuId;

    private String title;

    //是否选了
    private Boolean check;

    @Builder.Default
    private List<RoleMenuVo> children = new ArrayList<>();
}
