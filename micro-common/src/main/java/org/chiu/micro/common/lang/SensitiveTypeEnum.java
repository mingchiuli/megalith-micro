package org.chiu.micro.common.lang;


public enum SensitiveTypeEnum {

    TITLE(1, "title"),

    DESCRIPTION(2, "description"),

    CONTENT(3, "content");

    private final Integer code;

    private final String description;

    SensitiveTypeEnum(Integer code, String description) {
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
