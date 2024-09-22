package org.chiu.micro.user.provider;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.service.RoleService;
import org.chiu.micro.user.service.UserRoleService;
import org.chiu.micro.user.service.UserService;
import org.chiu.micro.user.vo.RoleEntityRpcVo;
import org.chiu.micro.user.vo.UserEntityRpcVo;
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
public class UserProvider {

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

    @GetMapping("/status/{username}/{status}")
    public Result<Void> changeUserStatusByUsername(@PathVariable String username, @PathVariable Integer status) {
        return Result.success(() -> userService.changeUserStatusByUsername(username, status));
    }

    @PostMapping("/role/{status}")
    public Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @PathVariable Integer status) {
        return Result.success(() -> roleService.findByRoleCodeInAndStatus(roles, status));
    }

    @PostMapping("/login/time/{username}")
    public Result<Void> updateLoginTime(@PathVariable String username) {
        return Result.success(() -> userService.updateLoginTime(username, LocalDateTime.now()));
    }

    @GetMapping("/email/{email}")
    Result<UserEntityRpcVo> findByEmail(@PathVariable String email) {
        return Result.success(() -> userService.findByEmail(email));
    }

    @GetMapping("/phone/{phone}")
    Result<UserEntityRpcVo> findByPhone(@PathVariable String phone) {
        return Result.success(() -> userService.findByPhone(phone));
    }

    @GetMapping("/role/{userId}")
    Result<List<String>> findRoleCodesDByUserId(@PathVariable Long userId) {
        return Result.success(() -> userRoleService.findRoleCodesByUserId(userId));
    }

    @GetMapping("/login/query/{username}")
    Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(@PathVariable String username) {
        return Result.success(() -> userService.findByUsernameOrEmailOrPhone(username));
    }

    @GetMapping("/unlock")
    Result<Void> unlock() {
        return Result.success(userService::unlockUser);
    }
}
