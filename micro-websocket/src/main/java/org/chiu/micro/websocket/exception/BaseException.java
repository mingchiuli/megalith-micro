package org.chiu.micro.websocket.exception;

import org.chiu.micro.websocket.lang.ExceptionMessage;

public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String message) {
        super(message);
        this.code = null;
    }

    public BaseException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMsg());
        this.code = exceptionMessage.getCode();
    }

    public Integer getCode() {
        return this.code;
    }
}
