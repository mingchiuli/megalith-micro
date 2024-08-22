package org.chiu.micro.exhibit.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    NORMAL(0, "正常状态"),

    HIDE(1, "隐藏/禁用状态"),

    SENSITIVE_FILTER(2, "过滤敏感内容状态");

    private final Integer code;

    private final String description;
}
