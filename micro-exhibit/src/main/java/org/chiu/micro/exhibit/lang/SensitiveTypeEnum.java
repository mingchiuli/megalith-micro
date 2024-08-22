package org.chiu.micro.exhibit.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SensitiveTypeEnum {

    TITLE(1, "title"),

    DESCRIPTION(2, "description"),
    
    CONTENT(3, "content");

    private final Integer code;

    private final String description;
}
