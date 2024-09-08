package org.chiu.micro.gateway.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_FOUND(1, "没有找到路由"),
    
    DATA_NOT_FOUND(2, "数据异常");

    private final Integer code;

    private final String msg;

}
