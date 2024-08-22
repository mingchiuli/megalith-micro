package org.chiu.micro.blog.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
}
