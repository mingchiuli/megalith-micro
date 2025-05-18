package wiki.chiu.micro.auth.rpc;

import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.vo.MenusAndButtonsRpcVo;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    private final MenuHttpService menuHttpService;

    private final AuthorityHttpService authorityHttpService;


    public UserHttpServiceWrapper(UserHttpService userHttpService, MenuHttpService menuHttpService, AuthorityHttpService authorityHttpService) {
        this.userHttpService = userHttpService;
        this.menuHttpService = menuHttpService;
        this.authorityHttpService = authorityHttpService;
    }

    public void changeUserStatusByUsername(String username, Integer status) {
        userHttpService.changeUserStatusByUsername(username, status);
    }

    public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
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

    public UserEntityRpcVo findById(Long userId) {
        return Result.handleResult(() -> userHttpService.findById(userId));
    }

    public UserEntityRpcVo findByUsernameOrEmailOrPhone(String username) {
        return Result.handleResult(() -> userHttpService.findByUsernameOrEmailOrPhone(username));
    }

    public Set<String> getAuthoritiesByRoleCode(String rawRole) {
        return Result.handleResult(() -> authorityHttpService.getAuthoritiesByRoleCode(rawRole));
    }

    public MenusAndButtonsRpcVo getCurrentUserNav(String rawRole) {
        return Result.handleResult(() -> menuHttpService.getCurrentUserNav(rawRole));
    }

    public List<AuthorityRpcVo> getSystemAuthorities() {
        return Result.handleResult(authorityHttpService::getAuthorities);
    }
}
