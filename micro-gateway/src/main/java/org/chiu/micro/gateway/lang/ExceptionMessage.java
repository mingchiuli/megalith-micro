package org.chiu.micro.gateway.lang;


public enum ExceptionMessage {

    NO_FOUND(1, "重新登录");

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
