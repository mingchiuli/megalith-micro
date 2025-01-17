package wiki.chiu.micro.user.controller;


import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.service.MenuService;
import wiki.chiu.micro.user.valid.DeleteMenu;
import wiki.chiu.micro.user.valid.MenuValue;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-12-04 2:22 am
 */
@RestController
@RequestMapping(value = "/sys/menu")
@Validated
public class MenuController {

    private final MenuService menuService;

    private final MenuAuthorityService menuAuthorityService;

    public MenuController(MenuService menuService, MenuAuthorityService menuAuthorityService) {
        this.menuService = menuService;
        this.menuAuthorityService = menuAuthorityService;
    }

    @GetMapping("/info/{id}")
    public Result<MenuEntityVo> info(@PathVariable Long id) {
        return Result.success(() -> menuService.findById(id));
    }

    @GetMapping("/list")
    public Result<List<MenuDisplayVo>> list() {
        return Result.success(menuService::tree);
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @MenuValue MenuEntityReq menu) {
        return Result.success(() -> menuService.saveOrUpdate(menu));
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable @DeleteMenu Long id) {
        return Result.success(() -> menuService.delete(id));
    }

    @GetMapping("/download")
    public byte[] download() {
        return menuService.download();
    }

    @PostMapping("/authority/{menuId}")
    public Result<Void> saveAuthority(@PathVariable Long menuId,
                                      @RequestBody List<Long> authorityIds) {
        return Result.success(() -> menuAuthorityService.saveAuthority(menuId, authorityIds));
    }

    @GetMapping("/authority/{menuId}")
    public Result<List<MenuAuthorityVo>> getAuthoritiesInfo(@PathVariable Long menuId) {
        return Result.success(() -> menuAuthorityService.getAuthoritiesInfo(menuId));
    }

}
