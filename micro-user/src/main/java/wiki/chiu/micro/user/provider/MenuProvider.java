package wiki.chiu.micro.user.provider;

import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.user.service.RoleMenuService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/inner/menu")
@Validated
public class MenuProvider implements MenuHttpService {

    private final RoleMenuService roleMenuService;

    public MenuProvider(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/nav")
    public Result<List<MenuRpcVo>> getCurrentUserNav(@RequestParam String role) {
        return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
    }
}
