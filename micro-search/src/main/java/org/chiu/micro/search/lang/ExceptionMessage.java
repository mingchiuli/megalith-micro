package org.chiu.micro.search.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

   

    NO_FOUND(1, "没有找到内容");

    private final Integer code;

    private final String msg;

}
