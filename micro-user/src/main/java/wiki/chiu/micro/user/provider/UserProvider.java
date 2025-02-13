package wiki.chiu.micro.user.provider;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * UserProvider
 */
@RestController
@RequestMapping(value = "/inner/user")
@Validated
public class UserProvider implements UserHttpService {

    private final UserService userService;

    private final RoleService roleService;

    private final UserRoleService userRoleService;

    public UserProvider(UserService userService, RoleService roleService, UserRoleService userRoleService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }


    @GetMapping("/{userId}")
    public Result<UserEntityRpcVo> findById(@PathVariable Long userId) {
        return Result.success(() -> userService.findById(userId));
    }

    @GetMapping("/status")
    public Result<Void> changeUserStatusByUsername(@RequestParam String username, @RequestParam Integer status) {
        return Result.success(() -> userService.changeUserStatusByUsername(username, status));
    }

    @PostMapping("/role")
    public Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @RequestParam Integer status) {
        return Result.success(() -> roleService.findByRoleCodeInAndStatus(roles, status));
    }

    @PostMapping("/login/time")
    public Result<Void> updateLoginTime(@RequestParam String username) {
        return Result.success(() -> userService.updateLoginTime(username, LocalDateTime.now()));
    }

    @GetMapping("/email")
    public Result<UserEntityRpcVo> findByEmail(@RequestParam String email) {
        return Result.success(() -> userService.findByEmail(email));
    }

    @GetMapping("/phone")
    public Result<UserEntityRpcVo> findByPhone(@RequestParam String phone) {
        return Result.success(() -> userService.findByPhone(phone));
    }

    @GetMapping("/role/{userId}")
    public Result<List<String>> findRoleCodesByUserId(@PathVariable Long userId) {
        return Result.success(() -> userRoleService.findRoleCodesByUserId(userId));
    }

    @GetMapping("/login/query")
    public Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(@RequestParam String username) {
        return Result.success(() -> userService.findByUsernameOrEmailOrPhone(username));
    }

    @GetMapping("/unlock")
    public Result<Void> unlock() {
        return Result.success(userService::unlockUser);
    }
}
