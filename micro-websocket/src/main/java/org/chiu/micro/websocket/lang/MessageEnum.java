package org.chiu.micro.websocket.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageEnum {

    PUSH_ALL(-1L, "推"),

    PULL_ALL(-2L, "拉");

    private final Long code;

    private final String description;
}
