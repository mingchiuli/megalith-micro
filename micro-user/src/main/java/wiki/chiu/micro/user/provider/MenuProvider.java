package wiki.chiu.micro.user.provider;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.vo.MenusAndButtonsRpcVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inner/menu")
@Validated
public class MenuProvider {

    private final RoleMenuService roleMenuService;

    public MenuProvider(RoleMenuService roleMenuService) {
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/nav/{role}")
    public Result<MenusAndButtonsRpcVo> nav(@PathVariable String role) {
        return Result.success(() -> roleMenuService.getCurrentRoleNav(role));
    }
}
