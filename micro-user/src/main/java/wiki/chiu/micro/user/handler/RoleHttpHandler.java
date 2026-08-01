package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
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
    private final ValidatedRequest validation;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public RoleHttpHandler(RoleService roleService, RoleMenuService roleMenuService,
                           ValidatedRequest validation) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
        this.validation = validation;
    }

    public ServerResponse info(ServerRequest request) {
        Long id = pathVariable(request, "id", Long::valueOf);
        return ok(Result.success(() -> roleService.info(id)));
    }

    public ServerResponse getPage(ServerRequest request) {
        Integer currentPage = optionalParam(request, "currentPage", 1, Integer::valueOf);
        Integer size = optionalParam(request, "size", 5, Integer::valueOf);
        return ok(Result.success(() -> roleService.getPage(currentPage, size)));
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        RoleEntityReq role = validation.body(request, RoleEntityReq.class);
        return ok(Result.success(() -> roleService.saveOrUpdate(role)));
    }

    public ServerResponse delete(ServerRequest request) throws Exception {
        List<Long> ids = validation.notEmpty(validation.body(request, LONG_LIST), "ids");
        return ok(Result.success(() -> roleService.delete(ids)));
    }

    public ServerResponse saveMenu(ServerRequest request) throws Exception {
        Long roleId = pathVariable(request, "roleId", Long::valueOf);
        List<Long> menuIds = validation.body(request, LONG_LIST);
        return ok(Result.success(() -> roleMenuService.saveMenu(roleId, menuIds)));
    }

    public ServerResponse getMenusInfo(ServerRequest request) {
        Long roleId = pathVariable(request, "roleId", Long::valueOf);
        return ok(Result.success(() -> roleMenuService.getMenusInfo(roleId)));
    }

    public ServerResponse download(ServerRequest request) {
        return ok(roleService.download());
    }

    public ServerResponse getValidAll(ServerRequest request) {
        return ok(Result.success(roleService::getValidAll));
    }
}
