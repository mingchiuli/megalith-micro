package wiki.chiu.micro.user.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.api.AuthorityHttpService;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.service.AuthorityService;

@Component
public class AuthorityInternalHttpHandler implements AuthorityHttpService {

  private final AuthorityService authorityService;

  public AuthorityInternalHttpHandler(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  public ServerResponse getAuthorities(ServerRequest request) {
    return ok(getAuthorities());
  }

  @Override
  public Result<List<AuthorityRpcVo>> getAuthorities() {
    return Result.success(() -> authorityService.findAllByService());
  }
}
