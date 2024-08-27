package org.chiu.micro.gateway.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String message) {
        super(message);
        this.code = null;
    }

}
