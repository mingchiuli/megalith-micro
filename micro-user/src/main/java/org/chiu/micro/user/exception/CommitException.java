package org.chiu.micro.user.exception;

import org.chiu.micro.user.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2022-12-22 10:04 am
 */
public class CommitException extends BaseException {

    public CommitException(String message) {
        super(message);
    }

    public CommitException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
