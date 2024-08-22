package org.chiu.micro.search.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    NORMAL(0, "正常状态"),

    HIDE(1, "隐藏/禁用状态");

    private final Integer code;

    private final String description;
}
