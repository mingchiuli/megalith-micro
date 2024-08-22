package org.chiu.micro.exhibit.exception;

import lombok.Getter;
import org.chiu.micro.exhibit.lang.ExceptionMessage;

@Getter
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
}
