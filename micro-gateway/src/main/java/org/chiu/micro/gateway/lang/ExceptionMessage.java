package org.chiu.micro.gateway.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_FOUND(1, "重新登录");

    private final Integer code;

    private final String msg;

}
