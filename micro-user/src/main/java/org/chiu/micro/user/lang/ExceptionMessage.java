package org.chiu.micro.user.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_AUTH(0, "没有权限"),

    NO_FOUND(1, "没有找到内容"),

    UPLOAD_MISS(3, "上传出现错误"),

    USER_MISS(4, "用户没有找到"),

    MENU_NOT_EXIST(12, "menu不存在"),

    ROLE_NOT_EXIST(13, "role不存在"),

    USER_NOT_EXIST(14, "user不存在"),

    PASSWORD_REQUIRED(15, "需要密码"),

    EMAIL_NOT_EXIST(16, "email不存在"),

    PHONE_NOT_EXIST(30, "phone not exist"),

    MENU_INVALID_OPERATE(32, "先删除子菜单，不允许直接删除父菜单"),

    PASSWORD_DIFF(36, "账号密码不一致"),

    BUTTON_MUST_NOT_PARENT(37, "按钮不能有子元素"),

    MENU_CHILDREN_MUST_BE_BUTTON(38, "菜单子元素只能为按钮"),

    CATALOGUE_CHILD_MUST_NOT_BUTTON(39, "分类的子元素不能是按钮"),

    CATALOGUE_PARENT_MUST_PARENT(40, "分类的父元素只能是分类");

    private final Integer code;

    private final String msg;

}
