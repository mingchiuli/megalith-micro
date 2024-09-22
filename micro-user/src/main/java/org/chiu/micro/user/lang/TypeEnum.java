package org.chiu.micro.user.lang;


public enum TypeEnum {

    CATALOGUE(0, "分类"),

    MENU(1, "菜单"),

    BUTTON(2, "按钮");

    private final Integer code;

    private final String description;

    TypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }


    public static TypeEnum getInstance(Integer code) {
        for (TypeEnum value : TypeEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
