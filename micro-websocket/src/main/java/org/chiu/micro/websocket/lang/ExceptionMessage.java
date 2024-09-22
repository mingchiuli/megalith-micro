package org.chiu.micro.websocket.lang;



public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    EDIT_NO_AUTH(2, "非法编辑");

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
