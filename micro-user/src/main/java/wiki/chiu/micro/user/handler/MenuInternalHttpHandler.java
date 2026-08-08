package wiki.chiu.micro.user.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.user.service.RoleMenuService;

@Component
public class MenuInternalHttpHandler implements MenuHttpService {

  private final RoleMenuService roleMenuService;

  public MenuInternalHttpHandler(RoleMenuService roleMenuService) {
    this.roleMenuService = roleMenuService;
  }

  public ServerResponse getCurrentUserNav(ServerRequest request) {
    return ok(getCurrentUserNav(requiredParam(request, "role")));
  }

  @Override
  public Result<List<MenuRpcVo>> getCurrentUserNav(String role) {
    return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
  }
}
