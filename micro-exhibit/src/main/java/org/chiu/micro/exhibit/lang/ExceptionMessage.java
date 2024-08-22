package org.chiu.micro.exhibit.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),

    TOKEN_INVALID(8, "token非法"),

    AUTH_EXCEPTION(10, "认证异常");

    private final Integer code;

    private final String msg;

}
