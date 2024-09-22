package org.chiu.micro.exhibit.lang;


public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    TOKEN_INVALID(8, "token非法"),

    AUTH_EXCEPTION(10, "认证异常");

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
