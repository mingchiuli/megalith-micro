package org.chiu.micro.common.lang;



public enum MessageActionFieldEnum {

    TITLE("title"),

    DESCRIPTION("description"),

    USER_ID("userId"),

    CONTENT("content"),

    LINK("link"),

    STATUS("status"),

    ID("id"),

    VERSION("version"),

    SENSITIVE_CONTENT_LIST("sensitiveContentList");

    private final String msg;

    MessageActionFieldEnum(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }
}
