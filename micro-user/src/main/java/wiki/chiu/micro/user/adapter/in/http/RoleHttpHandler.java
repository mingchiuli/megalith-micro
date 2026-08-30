package wiki.chiu.micro.user.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

import java.util.List;
import java.util.Objects;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.application.port.in.RoleDataPermissionService;
import wiki.chiu.micro.user.application.port.in.RoleMenuService;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.config.convertor.UserRequestConvertor;
import wiki.chiu.micro.user.req.RoleEntityReq;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:28 pm
 */
@Component
public class RoleHttpHandler {

  private final RoleService roleService;

  private final RoleMenuService roleMenuService;
  private final RoleDataPermissionService roleDataPermissionService;
  private final ValidatedRequest v;

  private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
      new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<List<DataPermissionEnum>> DATA_PERMISSION_LIST =
      new ParameterizedTypeReference<>() {};

  public RoleHttpHandler(
      RoleService roleService,
      RoleMenuService roleMenuService,
      RoleDataPermissionService roleDataPermissionService,
      ValidatedRequest v) {
    this.roleService = roleService;
    this.roleMenuService = roleMenuService;
    this.roleDataPermissionService = roleDataPermissionService;
    this.v = v;
  }

  public ServerResponse info(ServerRequest request) {
    Long id = v.positive(pathVariable(request, "id", Long::valueOf), "id");
    return ok(Result.success(() -> roleService.info(id)));
  }

  public ServerResponse getPage(ServerRequest request) {
    Integer currentPage =
        v.positive(optionalParam(request, "currentPage", 1, Integer::valueOf), "currentPage");
    Integer size = v.positive(optionalParam(request, "size", 5, Integer::valueOf), "size");
    return ok(Result.success(() -> roleService.getPage(currentPage, size)));
  }

  public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
    RoleEntityReq role = UserRequestConvertor.toRoleEntityReq(request);
    return ok(Result.success(() -> roleService.saveOrUpdate(role)));
  }

  public ServerResponse delete(ServerRequest request) throws Exception {
    List<Long> ids = v.notEmpty(v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
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

  public ServerResponse saveDataPermissions(ServerRequest request) throws Exception {
    Long roleId = v.positive(pathVariable(request, "roleId", Long::valueOf), "roleId");
    List<DataPermissionEnum> dataPermissions =
        v.notNull(request.body(DATA_PERMISSION_LIST), "dataPermissions");
    if (dataPermissions.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("dataPermissions must not contain null");
    }
    return ok(
        Result.success(
            () -> roleDataPermissionService.saveDataPermissions(roleId, dataPermissions)));
  }

  public ServerResponse getDataPermissions(ServerRequest request) {
    Long roleId = v.positive(pathVariable(request, "roleId", Long::valueOf), "roleId");
    return ok(Result.success(() -> roleDataPermissionService.getDataPermissions(roleId)));
  }

  public ServerResponse download(ServerRequest request) {
    return ok(roleService.download());
  }

  public ServerResponse getValidAll(ServerRequest request) {
    return ok(Result.success(roleService::getValidAll));
  }
}
