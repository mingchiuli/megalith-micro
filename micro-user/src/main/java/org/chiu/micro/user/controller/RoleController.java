package org.chiu.micro.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.user.req.RoleEntityReq;
import org.chiu.micro.user.service.RoleAuthorityService;
import org.chiu.micro.user.service.RoleMenuService;
import org.chiu.micro.user.service.RoleService;
import org.chiu.micro.user.vo.RoleAuthorityVo;
import org.chiu.micro.user.vo.RoleEntityVo;
import org.chiu.micro.user.vo.RoleMenuVo;
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

    private final RoleAuthorityService roleAuthorityService;

    public RoleController(RoleService roleService, RoleMenuService roleMenuService, RoleAuthorityService roleAuthorityService) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
        this.roleAuthorityService = roleAuthorityService;
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

    @PostMapping("/authority/{roleId}")
    public Result<Void> saveAuthority(@PathVariable Long roleId,
                                      @RequestBody List<Long> authorityIds) {
        return Result.success(() -> roleAuthorityService.saveAuthority(roleId, authorityIds));
    }

    @GetMapping("/authority/{roleId}")
    public Result<List<RoleAuthorityVo>> getAuthoritiesInfo(@PathVariable Long roleId) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesInfo(roleId));
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
