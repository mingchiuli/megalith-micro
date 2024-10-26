package wiki.chiu.micro.blog.rpc;


import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.exception.MissException;
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
        Result<UserEntityRpcDto> result = userHttpService.findById(userId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}
