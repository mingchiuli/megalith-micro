package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.ErrorCodes;
import wiki.chiu.micro.common.lang.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2022-07-07 11:06 AM
 */
public class MissException extends BaseException {

    public MissException(String message) {
        super(ErrorCodes.findByMessage(message).orElse(ExceptionMessage.NO_FOUND), message);
    }

    public MissException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
