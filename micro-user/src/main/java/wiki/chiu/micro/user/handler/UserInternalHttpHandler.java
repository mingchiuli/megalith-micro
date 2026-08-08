package wiki.chiu.micro.user.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;

/** Internal user HTTP handler. */
@Component
public class UserInternalHttpHandler implements UserHttpService {

  private final UserService userService;

  private final RoleService roleService;

  private final UserRoleService userRoleService;
  private final ValidatedRequest v;

  private static final ParameterizedTypeReference<List<String>> STRING_LIST =
      new ParameterizedTypeReference<>() {};

  public UserInternalHttpHandler(
      UserService userService,
      RoleService roleService,
      UserRoleService userRoleService,
      ValidatedRequest v) {
    this.userService = userService;
    this.roleService = roleService;
    this.userRoleService = userRoleService;
    this.v = v;
  }

  public ServerResponse findById(ServerRequest request) {
    Long userId = v.positive(pathVariable(request, "userId", Long::valueOf), "userId");
    return ok(findById(userId));
  }

  public ServerResponse changeUserStatusByUsername(ServerRequest request) {
    return ok(
        changeUserStatusByUsername(
            requiredParam(request, "username"),
            v.range(requiredParam(request, "status", Integer::valueOf), 0, 1, "status")));
  }

  public ServerResponse findByRoleCodeInAndStatus(ServerRequest request) throws Exception {
    return ok(
        findByRoleCodeInAndStatus(
            v.notBlankElements(request.body(STRING_LIST), "roles"),
            v.range(requiredParam(request, "status", Integer::valueOf), 0, 1, "status")));
  }

  public ServerResponse updateLoginTime(ServerRequest request) {
    return ok(updateLoginTime(requiredParam(request, "username")));
  }

  public ServerResponse findByEmail(ServerRequest request) {
    return ok(findByEmail(requiredParam(request, "email")));
  }

  public ServerResponse findByPhone(ServerRequest request) {
    return ok(findByPhone(requiredParam(request, "phone")));
  }

  public ServerResponse findRoleCodesByUserId(ServerRequest request) {
    Long userId = v.positive(pathVariable(request, "userId", Long::valueOf), "userId");
    return ok(findRoleCodesByUserId(userId));
  }

  public ServerResponse findByUsernameOrEmailOrPhone(ServerRequest request) {
    return ok(findByUsernameOrEmailOrPhone(requiredParam(request, "username")));
  }

  @Override
  public Result<UserEntityRpcVo> findById(Long userId) {
    return Result.success(() -> userService.findById(userId));
  }

  @Override
  public Result<Void> changeUserStatusByUsername(String username, Integer status) {
    return Result.success(() -> userService.changeUserStatusByUsername(username, status));
  }

  @Override
  public Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(
      List<String> roles, Integer status) {
    return Result.success(() -> roleService.findByRoleCodeInAndStatus(roles, status));
  }

  @Override
  public Result<Void> updateLoginTime(String username) {
    return Result.success(() -> userService.updateLoginTime(username, LocalDateTime.now()));
  }

  @Override
  public Result<UserEntityRpcVo> findByEmail(String email) {
    return Result.success(() -> userService.findByEmail(email));
  }

  @Override
  public Result<UserEntityRpcVo> findByPhone(String phone) {
    return Result.success(() -> userService.findByPhone(phone));
  }

  @Override
  public Result<List<String>> findRoleCodesByUserId(Long userId) {
    return Result.success(() -> userRoleService.findRoleCodesByUserId(userId));
  }

  @Override
  public Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(String username) {
    return Result.success(() -> userService.findByUsernameOrEmailOrPhone(username));
  }
}
