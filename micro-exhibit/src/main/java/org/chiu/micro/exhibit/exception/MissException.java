package org.chiu.micro.exhibit.exception;

import org.chiu.micro.exhibit.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2022-07-07 11:06 AM
 */
public class MissException extends BaseException {

    public MissException(String message) {
        super(message);
    }

    public MissException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
