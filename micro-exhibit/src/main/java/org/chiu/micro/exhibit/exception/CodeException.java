package org.chiu.micro.exhibit.exception;

import org.chiu.micro.exhibit.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2023-04-03 1:48 am
 */
public class CodeException extends BaseException {

    public CodeException(String message) {
        super(message);
    }

    public CodeException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
