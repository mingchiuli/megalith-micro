package org.chiu.micro.user.exception;

import org.chiu.micro.user.lang.ExceptionMessage;


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
