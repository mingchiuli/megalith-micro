package org.chiu.micro.websocket.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    NO_FOUND(1, "没有找到内容"),
    
    EDIT_NO_AUTH(2, "非法编辑");

    private final Integer code;

    private final String msg;

}
