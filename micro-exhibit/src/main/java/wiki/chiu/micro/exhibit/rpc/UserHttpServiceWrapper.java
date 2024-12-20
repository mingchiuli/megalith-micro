package wiki.chiu.micro.exhibit.rpc;

import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.stereotype.Component;


@Component
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    public UserHttpServiceWrapper(UserHttpService userHttpService) {
        this.userHttpService = userHttpService;
    }

    public UserEntityRpcDto findById(Long userId) {
        return Result.handleResult(() -> userHttpService.findById(userId));
    }
}
