package org.chiu.micro.user.provider;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.service.RoleAuthorityService;
import org.chiu.micro.user.service.RoleService;
import org.chiu.micro.user.service.UserRoleService;
import org.chiu.micro.user.service.UserService;
import org.chiu.micro.user.vo.RoleEntityRpcVo;
import org.chiu.micro.user.vo.UserEntityRpcVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.time.LocalDateTime;


/**
 * UserProvider
 */
@RestController
@RequestMapping(value = "/inner/user")
@RequiredArgsConstructor
@Validated
public class UserProvider {

    private final UserService userService;

    private final RoleService roleService;

    private final UserRoleService userRoleService;

    private final RoleAuthorityService roleAuthorityService;

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
    void updateLoginTime(@PathVariable String username) {
        userService.updateLoginTime(username, LocalDateTime.now());
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

    @PostMapping("/role/authority")
    Result<List<String>> getAuthoritiesByRoleCodes(@RequestBody @NotBlank String rawRoles) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRoles));
    }
}