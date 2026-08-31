package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.CommonErrorCode;

public final class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(CommonErrorCode.VALIDATION_ERROR, message);
    }
}
