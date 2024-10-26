package wiki.chiu.micro.common.lang;


import wiki.chiu.micro.common.exception.MissException;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

public enum AuthMenuOperateEnum {

    AUTH(1, "权限"),

    MENU(2, "菜单或按钮"),

    AUTH_AND_MENU(3, "权限和菜单");

    private final Integer type;

    private final String description;

    AuthMenuOperateEnum(Integer type, String description) {
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
