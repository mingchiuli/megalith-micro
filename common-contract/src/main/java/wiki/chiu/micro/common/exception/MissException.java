package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.error.ErrorCodes;
import wiki.chiu.micro.common.error.ExceptionMessage;

/**
 * @author mingchiuli
 * @create 2022-07-07 11:06 AM
 */
public class MissException extends BaseException {

    private static final long serialVersionUID = 1L;

    public MissException(String message) {
        super(ErrorCodes.findByMessage(message).orElse(ExceptionMessage.NO_FOUND), message);
    }

    public MissException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
