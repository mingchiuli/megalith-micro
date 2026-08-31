package wiki.chiu.micro.common.rpc;

import java.util.function.Supplier;

import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ErrorCode;
import wiki.chiu.micro.common.lang.ErrorCodes;
import wiki.chiu.micro.common.lang.Result;

public final class RemoteResult {

    private static final int SUCCESS_CODE = 200;

    private RemoteResult() {
    }

    public static <T> T requireSuccess(Supplier<Result<T>> call) {
        Result<T> result = call.get();
        if (result == null) {
            throw new RemoteServiceException(
                CommonErrorCode.DOWNSTREAM_ERROR, "downstream service returned an empty response", 0);
        }
        if (result.code() == null || result.code() != SUCCESS_CODE) {
            int code = result.code() == null ? CommonErrorCode.DOWNSTREAM_ERROR.code() : result.code();
            ErrorCode errorCode = ErrorCodes.find(code).orElse(CommonErrorCode.DOWNSTREAM_ERROR);
            throw new RemoteServiceException(errorCode, result.msg(), SUCCESS_CODE);
        }
        return result.data();
    }
}
