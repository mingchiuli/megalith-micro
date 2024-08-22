package org.chiu.micro.user.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeEnum {

    CATALOGUE(0, "分类"),

    MENU(1, "菜单"),

    BUTTON(2, "按钮");

    private final Integer code;

    private final String description;


    public static TypeEnum getInstance(Integer code) {
        for (TypeEnum value : TypeEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }
}
