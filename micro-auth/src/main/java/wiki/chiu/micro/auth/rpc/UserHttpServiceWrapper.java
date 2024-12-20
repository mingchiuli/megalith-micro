package wiki.chiu.micro.auth.rpc;

import wiki.chiu.micro.common.dto.AuthorityRpcDto;
import wiki.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import wiki.chiu.micro.common.dto.RoleEntityRpcDto;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    public UserHttpServiceWrapper(UserHttpService userHttpService) {
        this.userHttpService = userHttpService;
    }

    public void changeUserStatusByUsername(String username, Integer status) {
        userHttpService.changeUserStatusByUsername(username, status);
    }

    public List<RoleEntityRpcDto> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        return Result.handleResult(() -> userHttpService.findByRoleCodeInAndStatus(roles, status));
    }

    public void updateLoginTime(String username) {
        Result.handleResult(() -> userHttpService.updateLoginTime(username));
    }

    public void unlock() {
        Result.handleResult(userHttpService::unlock);
    }

    public void findByEmail(String loginEmail) {
        Result.handleResult(() -> userHttpService.findByEmail(loginEmail));
    }

    public void findByPhone(String loginSMS) {
        Result.handleResult(() -> userHttpService.findByPhone(loginSMS));
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        return Result.handleResult(() -> userHttpService.findRoleCodesByUserId(userId));
    }

    public UserEntityRpcDto findById(Long userId) {
        return Result.handleResult(() -> userHttpService.findById(userId));
    }

    public UserEntityRpcDto findByUsernameOrEmailOrPhone(String username) {
        return Result.handleResult(() -> userHttpService.findByUsernameOrEmailOrPhone(username));
    }

    public Set<String> getAuthoritiesByRoleCode(String rawRole) {
        return Result.handleResult(() -> userHttpService.getAuthoritiesByRoleCode(rawRole));
    }

    public MenusAndButtonsRpcDto getCurrentUserNav(String rawRole) {
        return Result.handleResult(() -> userHttpService.getCurrentUserNav(rawRole));
    }

    public List<AuthorityRpcDto> getSystemAuthorities(List<String> serviceHost) {
        return Result.handleResult(() -> userHttpService.getAuthorities(serviceHost));
    }
}
