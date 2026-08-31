package wiki.chiu.micro.common.lang;

public enum CommonErrorCode implements ErrorCode {
    VALIDATION_ERROR(1000, "request validation failed", ErrorCategory.VALIDATION),
    MALFORMED_REQUEST(1001, "request body is malformed", ErrorCategory.VALIDATION),
    CONFLICT(2000, "resource state conflict", ErrorCategory.CONFLICT),
    DOWNSTREAM_ERROR(9000, "downstream service failed", ErrorCategory.UPSTREAM),
    DOWNSTREAM_TIMEOUT(9001, "downstream service timed out", ErrorCategory.TIMEOUT),
    INTERNAL_ERROR(9999, "internal server error", ErrorCategory.INTERNAL);

    private final int code;
    private final String defaultMessage;
    private final ErrorCategory category;

    CommonErrorCode(int code, String defaultMessage, ErrorCategory category) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.category = category;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
