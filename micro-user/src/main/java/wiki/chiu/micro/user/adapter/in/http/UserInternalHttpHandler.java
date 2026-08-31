package wiki.chiu.micro.user.adapter.in.http;

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
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.api.UserHttpService;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;

/**
 * Internal user HTTP handler.
 */
@Component
public class UserInternalHttpHandler implements UserHttpService {

    private final UserIdentityService userIdentityService;

    private final RoleService roleService;

    private final ValidatedRequest v;

    private static final ParameterizedTypeReference<List<String>> STRING_LIST =
        new ParameterizedTypeReference<>() {
        };

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
        new ParameterizedTypeReference<>() {
        };

    public UserInternalHttpHandler(
        UserIdentityService userIdentityService, RoleService roleService, ValidatedRequest v) {
        this.userIdentityService = userIdentityService;
        this.roleService = roleService;
        this.v = v;
    }

    public ServerResponse findById(ServerRequest request) {
        Long userId = v.positive(pathVariable(request, "userId", Long::valueOf), "userId");
        return ok(findById(userId));
    }

    public ServerResponse lockAfterPasswordFailures(ServerRequest request) {
        Long userId = v.positive(pathVariable(request, "userId", Long::valueOf), "userId");
        return ok(lockAfterPasswordFailures(userId));
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

    public ServerResponse findUserAccess(ServerRequest request) {
        Long userId = v.positive(pathVariable(request, "userId", Long::valueOf), "userId");
        return ok(findUserAccess(userId));
    }

    public ServerResponse findAllRoleAuthorizations(ServerRequest request) {
        return ok(findAllRoleAuthorizations());
    }

    public ServerResponse findRoleAuthorizations(ServerRequest request) throws Exception {
        return ok(findRoleAuthorizations(v.positiveElements(request.body(LONG_LIST), "roleIds")));
    }

    public ServerResponse findByUsernameOrEmailOrPhone(ServerRequest request) {
        return ok(findByUsernameOrEmailOrPhone(requiredParam(request, "username")));
    }

    @Override
    public Result<UserEntityRpcVo> findById(Long userId) {
        return Result.success(() -> userIdentityService.findById(userId));
    }

    @Override
    public Result<Void> lockAfterPasswordFailures(Long userId) {
        return Result.success(() -> userIdentityService.lockAfterPasswordFailures(userId));
    }

    @Override
    public Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(
        List<String> roles, Integer status) {
        return Result.success(() -> roleService.findByRoleCodeInAndStatus(roles, status));
    }

    @Override
    public Result<Void> updateLoginTime(String username) {
        return Result.success(() -> userIdentityService.updateLoginTime(username, LocalDateTime.now()));
    }

    @Override
    public Result<UserEntityRpcVo> findByEmail(String email) {
        return Result.success(() -> userIdentityService.findByEmail(email));
    }

    @Override
    public Result<UserEntityRpcVo> findByPhone(String phone) {
        return Result.success(() -> userIdentityService.findByPhone(phone));
    }

    @Override
    public Result<UserAccessRpcVo> findUserAccess(Long userId) {
        return Result.success(() -> userIdentityService.findUserAccess(userId));
    }

    @Override
    public Result<List<RoleAuthorizationRpcVo>> findAllRoleAuthorizations() {
        return Result.success(roleService::findAllRoleAuthorizations);
    }

    @Override
    public Result<List<RoleAuthorizationRpcVo>> findRoleAuthorizations(List<Long> roleIds) {
        return Result.success(() -> roleService.findRoleAuthorizations(roleIds));
    }

    @Override
    public Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(String username) {
        return Result.success(() -> userIdentityService.findByLogin(username));
    }
}
