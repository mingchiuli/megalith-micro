package wiki.chiu.micro.user.handler;


import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.service.MenuService;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-12-04 2:22 am
 */
@Component
public class MenuHttpHandler {

    private final MenuService menuService;

    private final MenuAuthorityService menuAuthorityService;

    public MenuHttpHandler(MenuService menuService, MenuAuthorityService menuAuthorityService) {
        this.menuService = menuService;
        this.menuAuthorityService = menuAuthorityService;
    }

    public Result<MenuEntityVo> info(Long id) {
        return Result.success(() -> menuService.findById(id));
    }

    public Result<List<MenuDisplayVo>> list() {
        return Result.success(menuService::tree);
    }

    public Result<Void> saveOrUpdate(MenuEntityReq menu) {
        return Result.success(() -> menuService.saveOrUpdate(menu));
    }

    public Result<Void> delete(Long id) {
        return Result.success(() -> menuService.delete(id));
    }

    public byte[] download() {
        return menuService.download();
    }

    public Result<Void> saveAuthority(Long menuId, List<Long> authorityIds) {
        return Result.success(() -> menuAuthorityService.saveAuthority(menuId, authorityIds));
    }

    public Result<List<MenuAuthorityVo>> getAuthoritiesInfo(Long menuId) {
        return Result.success(() -> menuAuthorityService.getAuthoritiesInfo(menuId));
    }

}
