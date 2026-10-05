package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.error.CommonErrorCode;

public final class ValidationException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(CommonErrorCode.VALIDATION_ERROR, message);
    }
}
