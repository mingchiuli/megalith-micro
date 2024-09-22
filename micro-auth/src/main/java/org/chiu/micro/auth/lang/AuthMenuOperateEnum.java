package org.chiu.micro.auth.lang;

import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import org.chiu.micro.auth.exception.MissException;

public enum AuthMenuOperateEnum {

    AUTH(1, "权限"),

    MENU(2, "菜单或按钮"),

    AUTH_AND_MENU(3, "权限和菜单");

    private final Integer type;

    private final String description;

    private AuthMenuOperateEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public static AuthMenuOperateEnum of(Integer type) {
        for (AuthMenuOperateEnum operateEnum : values()) {
            if (operateEnum.type.equals(type)) {
                return operateEnum;
            }
        }

        throw new MissException(NO_FOUND);
    }

    public Integer getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }
}
