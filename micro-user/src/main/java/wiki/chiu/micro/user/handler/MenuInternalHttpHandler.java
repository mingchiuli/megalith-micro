package wiki.chiu.micro.user.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.user.service.RoleMenuService;

import java.util.List;

@Component
public class MenuInternalHttpHandler implements MenuHttpService {

    private final RoleMenuService roleMenuService;

    public MenuInternalHttpHandler(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @Override
    public Result<List<MenuRpcVo>> getCurrentUserNav(String role) {
        return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
    }
}
