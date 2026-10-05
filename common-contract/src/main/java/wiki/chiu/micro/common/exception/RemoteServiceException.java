package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.error.ErrorCode;

public final class RemoteServiceException extends BaseException {

    private static final long serialVersionUID = 1L;

    private final int upstreamStatus;

    public RemoteServiceException(ErrorCode errorCode, String message, int upstreamStatus) {
        super(errorCode, message);
        this.upstreamStatus = upstreamStatus;
    }

    public RemoteServiceException(String message, Throwable cause) {
        super(CommonErrorCode.DOWNSTREAM_ERROR, message, cause);
        this.upstreamStatus = 0;
    }

    public int upstreamStatus() {
        return upstreamStatus;
    }
}
