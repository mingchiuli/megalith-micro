package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:28 pm
 */
@Component
public class RoleHttpHandler {

    private final RoleService roleService;

    private final RoleMenuService roleMenuService;

    public RoleHttpHandler(RoleService roleService, RoleMenuService roleMenuService) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    public Result<RoleEntityVo> info(Long id) {
        return Result.success(() -> roleService.info(id));
    }

    public Result<PageAdapter<RoleEntityVo>> getPage(Integer currentPage, Integer size) {
        return Result.success(() -> roleService.getPage(currentPage, size));
    }

    public Result<Void> saveOrUpdate(RoleEntityReq role) {
        return Result.success(() -> roleService.saveOrUpdate(role));
    }

    public Result<Void> delete(List<Long> ids) {
        return Result.success(() -> roleService.delete(ids));
    }

    public Result<Void> saveMenu(Long roleId, List<Long> menuIds) {
        return Result.success(() -> roleMenuService.saveMenu(roleId, menuIds));
    }

    public Result<List<RoleMenuVo>> getMenusInfo(Long roleId) {
        return Result.success(() -> roleMenuService.getMenusInfo(roleId));
    }

    public byte[] download() {
        return roleService.download();
    }

    public Result<List<RoleEntityVo>> getValidAll() {
        return Result.success(roleService::getValidAll);
    }
}
