package wiki.chiu.micro.user.provider;

import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.vo.MenusAndButtonsRpcVo;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/inner/menu")
@Validated
public class MenuProvider {

    private final RoleMenuService roleMenuService;

    public MenuProvider(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/nav")
    public Result<MenusAndButtonsRpcVo> nav(@RequestParam String role) {
        return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
    }
}
