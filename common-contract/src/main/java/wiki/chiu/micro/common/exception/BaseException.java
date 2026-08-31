package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ErrorCode;
import wiki.chiu.micro.common.lang.ExceptionMessage;

public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(String message) {
        this(CommonErrorCode.INTERNAL_ERROR, message);
    }

    public BaseException(ExceptionMessage exceptionMessage) {
        this(exceptionMessage, exceptionMessage.defaultMessage());
    }

    public BaseException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
