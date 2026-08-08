package wiki.chiu.micro.exhibit.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;

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
