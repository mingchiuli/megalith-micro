package org.chiu.micro.auth.lang;


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

    RE_LOGIN(35, "重新登录");

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
