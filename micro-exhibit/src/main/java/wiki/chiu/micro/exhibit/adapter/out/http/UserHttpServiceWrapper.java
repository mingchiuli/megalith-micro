package wiki.chiu.micro.exhibit.adapter.out.http;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.user.api.UserHttpService;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

@Component
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    public UserHttpServiceWrapper(UserHttpService userHttpService) {
        this.userHttpService = userHttpService;
    }

    public UserEntityRpcVo findById(Long userId) {
        try {
            return RemoteResult.requireSuccess(() -> userHttpService.findById(userId));
        } catch (RemoteServiceException failure) {
            int code = failure.errorCode().code();
            if (code == ExceptionMessage.USER_MISS.code()
                || code == ExceptionMessage.USER_NOT_EXIST.code()) {
                throw new MissException(ExceptionMessage.NO_FOUND);
            }
            throw failure;
        }
    }
}
