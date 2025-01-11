package wiki.chiu.micro.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:28 pm
 */
@RestController
@RequestMapping(value = "/sys/role")
@Validated
public class RoleController {

    private final RoleService roleService;

    private final RoleMenuService roleMenuService;

    public RoleController(RoleService roleService, RoleMenuService roleMenuService) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    @GetMapping("/info/{id}")
    public Result<RoleEntityVo> info(@PathVariable Long id) {
        return Result.success(() -> roleService.info(id));
    }

    @GetMapping("/roles")
    public Result<PageAdapter<RoleEntityVo>> getPage(@RequestParam(defaultValue = "1") Integer currentPage,
                                                     @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(() -> roleService.getPage(currentPage, size));
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @Valid RoleEntityReq role) {
        return Result.success(() -> roleService.saveOrUpdate(role));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @NotEmpty List<Long> ids) {
        return Result.success(() -> roleService.delete(ids));
    }

    @PostMapping("/menu/{roleId}")
    public Result<Void> saveMenu(@PathVariable Long roleId,
                                 @RequestBody List<Long> menuIds) {
        return Result.success(() -> roleMenuService.saveMenu(roleId, menuIds));
    }

    @GetMapping("/menu/{roleId}")
    public Result<List<RoleMenuVo>> getMenusInfo(@PathVariable Long roleId) {
        return Result.success(() -> roleMenuService.getMenusInfo(roleId));
    }

    @GetMapping("/download")
    public byte[] download() {
        return roleService.download();
    }

    @GetMapping("/valid/all")
    public Result<List<RoleEntityVo>> getValidAll() {
        return Result.success(roleService::getValidAll);
    }
}
