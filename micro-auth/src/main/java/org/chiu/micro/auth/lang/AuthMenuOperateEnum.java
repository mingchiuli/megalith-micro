package org.chiu.micro.auth.lang;

import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import org.chiu.micro.auth.exception.MissException;

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

    public static AuthMenuOperateEnum of(Integer type) {
        for (AuthMenuOperateEnum operateEnum : values()) {
            if (operateEnum.type.equals(type)) {
                return operateEnum;
            }
        }

        throw new MissException(NO_FOUND);
    }
}
