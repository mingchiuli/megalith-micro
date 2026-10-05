package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.error.ExceptionMessage;

public class AuthException extends BaseException {

    private static final long serialVersionUID = 1L;

    public AuthException(String string) {
        super(ExceptionMessage.NO_AUTH, string);
    }
}
