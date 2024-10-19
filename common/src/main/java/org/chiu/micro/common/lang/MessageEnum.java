package org.chiu.micro.common.lang;


public enum MessageEnum {

    PUSH_ALL(-1, "推"),

    PULL_ALL(-2, "拉");

    private final Integer code;

    private final String description;

    MessageEnum(Integer code, String description) {
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
