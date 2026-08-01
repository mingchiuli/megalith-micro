package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Internal user HTTP handler.
 */
@Component
public class UserInternalHttpHandler implements UserHttpService {

    private final UserService userService;

    private final RoleService roleService;

    private final UserRoleService userRoleService;

    public UserInternalHttpHandler(UserService userService, RoleService roleService, UserRoleService userRoleService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
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
    public Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
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
