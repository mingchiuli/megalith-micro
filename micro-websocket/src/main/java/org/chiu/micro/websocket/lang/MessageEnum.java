package org.chiu.micro.websocket.lang;


public enum MessageEnum {

    PUSH_ALL(-1L, "推"),

    PULL_ALL(-2L, "拉");

    private final Long code;

    private final String description;

    MessageEnum(Long code, String description) {
        this.code = code;
        this.description = description;
    }

    public Long getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }
}
