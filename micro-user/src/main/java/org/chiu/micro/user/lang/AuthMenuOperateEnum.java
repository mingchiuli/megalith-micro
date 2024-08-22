package org.chiu.micro.user.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthMenuOperateEnum {
    
    AUTH(1, "权限"),

    MENU(2, "菜单或按钮"),
    
    AUTH_AND_MENU(3, "权限和菜单");

    private final Integer type;

    private final String description;
}
