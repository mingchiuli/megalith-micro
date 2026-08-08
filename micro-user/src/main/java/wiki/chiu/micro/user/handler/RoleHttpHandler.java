package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.converter.UserRequestConverter;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.service.RoleService;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:28 pm
 */
@Component
public class RoleHttpHandler {

    private final RoleService roleService;

    private final RoleMenuService roleMenuService;
    private final ValidatedRequest v;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public RoleHttpHandler(RoleService roleService, RoleMenuService roleMenuService,
                           ValidatedRequest v) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
        this.v = v;
    }

    public ServerResponse info(ServerRequest request) {
        Long id = v.positive(pathVariable(request, "id", Long::valueOf), "id");
        return ok(Result.success(() -> roleService.info(id)));
    }

    public ServerResponse getPage(ServerRequest request) {
        Integer currentPage = v.positive(optionalParam(request, "currentPage", 1, Integer::valueOf), "currentPage");
        Integer size = v.positive(optionalParam(request, "size", 5, Integer::valueOf), "size");
        return ok(Result.success(() -> roleService.getPage(currentPage, size)));
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        RoleEntityReq role = UserRequestConverter.toRoleEntityReq(request);
        return ok(Result.success(() -> roleService.saveOrUpdate(role)));
    }

    public ServerResponse delete(ServerRequest request) throws Exception {
        List<Long> ids = v.notEmpty(
                v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
        return ok(Result.success(() -> roleService.delete(ids)));
    }

    public ServerResponse saveMenu(ServerRequest request) throws Exception {
        Long roleId = v.positive(pathVariable(request, "roleId", Long::valueOf), "roleId");
        List<Long> menuIds = v.positiveElements(request.body(LONG_LIST), "menuIds");
        return ok(Result.success(() -> roleMenuService.saveMenu(roleId, menuIds)));
    }

    public ServerResponse getMenusInfo(ServerRequest request) {
        Long roleId = v.positive(pathVariable(request, "roleId", Long::valueOf), "roleId");
        return ok(Result.success(() -> roleMenuService.getMenusInfo(roleId)));
    }

    public ServerResponse download(ServerRequest request) {
        return ok(roleService.download());
    }

    public ServerResponse getValidAll(ServerRequest request) {
        return ok(Result.success(roleService::getValidAll));
    }
}
