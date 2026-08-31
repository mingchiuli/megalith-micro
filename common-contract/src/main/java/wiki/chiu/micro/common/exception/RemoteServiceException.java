package wiki.chiu.micro.common.exception;

import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ErrorCode;

public final class RemoteServiceException extends BaseException {

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
