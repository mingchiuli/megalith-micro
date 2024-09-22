package org.chiu.micro.websocket.lang;

public enum StatusEnum {

    NORMAL(0, "正常状态"),

    HIDE(1, "隐藏/禁用状态"),

    SENSITIVE_FILTER(2, "过滤敏感内容状态");

    private final Integer code;

    private final String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
