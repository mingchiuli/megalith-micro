package wiki.chiu.micro.blog.adapter.out.http;

import org.springframework.stereotype.Component;

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
        return RemoteResult.requireSuccess(() -> userHttpService.findById(userId));
    }
}
