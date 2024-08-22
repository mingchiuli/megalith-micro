package org.chiu.micro.search.exception;

import lombok.Getter;
import org.chiu.micro.search.lang.ExceptionMessage;

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
