package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2022-12-22 10:04 am
 */
public class CommitException extends BaseException {

    public CommitException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
