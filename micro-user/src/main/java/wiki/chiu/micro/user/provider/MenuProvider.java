package wiki.chiu.micro.user.provider;

import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.vo.MenusAndButtonsRpcVo;
import wiki.chiu.micro.user.service.RoleMenuService;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/inner/menu")
@Validated
public class MenuProvider implements MenuHttpService {

    private final RoleMenuService roleMenuService;

    public MenuProvider(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/nav")
    public Result<MenusAndButtonsRpcVo> getCurrentUserNav(@RequestParam String role) {
        return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
    }
}
