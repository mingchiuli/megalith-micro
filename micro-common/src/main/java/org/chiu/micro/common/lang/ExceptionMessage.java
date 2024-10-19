package org.chiu.micro.common.lang;


public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    TOKEN_INVALID(8, "token非法"),

    AUTH_EXCEPTION(10, "认证异常"),

    CODE_TRY_MAX(19, "code reach max try number"),

    CODE_EXPIRED(20, "code expired"),

    CODE_MISMATCH(21, "code mismatch"),

    CODE_NOT_EXIST(22, "code not exist"),

    CODE_EXISTED(23, "code existed"),

    PASSWORD_MISMATCH(24, "Failed to authenticate since password does not match stored value"),

    PASSWORD_MISS(25, "Failed to authenticate since no credentials provided"),

    SMS_TRY_MAX(26, "sms reach max try number"),

    SMS_EXPIRED(27, "sms expired"),

    SMS_MISMATCH(28, "sms mismatch"),

    SMS_NOT_EXIST(29, "sms not exist"),

    ROLE_DISABLED(31, "role disabled"),

    INVALID_LOGIN_OPERATE(33, "非法登录"),

    ACCOUNT_LOCKED(34, "账户被锁"),

    RE_LOGIN(35, "重新登录"),

    UPLOAD_MISS(36, "上传出现错误"),

    USER_MISS(37, "用户没有找到"),

    EDIT_NO_AUTH(38, "必须编辑自己的文章"),

    MENU_NOT_EXIST(39, "menu不存在"),

    ROLE_NOT_EXIST(40, "role不存在"),

    USER_NOT_EXIST(41, "user不存在"),

    PASSWORD_REQUIRED(42, "需要密码"),

    EMAIL_NOT_EXIST(43, "email不存在"),

    PHONE_NOT_EXIST(44, "phone not exist"),

    MENU_INVALID_OPERATE(45, "先删除子菜单，不允许直接删除父菜单"),

    PASSWORD_DIFF(46, "账号密码不一致"),

    BUTTON_MUST_NOT_PARENT(47, "按钮不能有子元素"),

    MENU_CHILDREN_MUST_BE_BUTTON(48, "菜单子元素只能为按钮"),

    CATALOGUE_CHILD_MUST_NOT_BUTTON(49, "分类的子元素不能是按钮"),

    CATALOGUE_PARENT_MUST_PARENT(50, "分类的父元素只能是分类"),

    NO_AUTH(51, "没有权限");

    private final Integer code;

    private final String msg;

    ExceptionMessage(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
