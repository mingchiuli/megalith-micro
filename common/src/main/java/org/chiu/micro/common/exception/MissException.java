package org.chiu.micro.common.exception;

import org.chiu.micro.common.lang.ExceptionMessage;

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
