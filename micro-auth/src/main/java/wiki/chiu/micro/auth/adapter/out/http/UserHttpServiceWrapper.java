package wiki.chiu.micro.auth.adapter.out.http;

import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.user.api.AuthorityHttpService;
import wiki.chiu.micro.user.api.MenuHttpService;
import wiki.chiu.micro.user.api.UserHttpService;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;

@Component
public class UserHttpServiceWrapper implements UserDirectory {

    private final UserHttpService userHttpService;

    private final MenuHttpService menuHttpService;

    private final AuthorityHttpService authorityHttpService;

    public UserHttpServiceWrapper(
        UserHttpService userHttpService,
        MenuHttpService menuHttpService,
        AuthorityHttpService authorityHttpService) {
        this.userHttpService = userHttpService;
        this.menuHttpService = menuHttpService;
        this.authorityHttpService = authorityHttpService;
    }

    @Override
    public UserAccount findById(Long userId) {
        return UserRpcMapper.toAccount(
            RemoteResult.requireSuccess(() -> userHttpService.findById(userId)));
    }

    @Override
    public UserAccount findByLoginName(String loginName) {
        return UserRpcMapper.toAccount(
            RemoteResult.requireSuccess(() -> userHttpService.findByUsernameOrEmailOrPhone(loginName)));
    }

    @Override
    public void findByEmail(String loginEmail) {
        RemoteResult.requireSuccess(() -> userHttpService.findByEmail(loginEmail));
    }

    @Override
    public void findByPhone(String loginSMS) {
        RemoteResult.requireSuccess(() -> userHttpService.findByPhone(loginSMS));
    }

    @Override
    public UserAccess findUserAccess(Long userId) {
        return UserRpcMapper.toAccess(
            RemoteResult.requireSuccess(() -> userHttpService.findUserAccess(userId)));
    }

    @Override
    public List<String> findEnabledRoleCodesOf(Long userId) {
        List<Long> roleIds = findUserAccess(userId).roleIds();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return enabledCodes(findRoleAuthorizations(roleIds));
    }

    @Override
    public List<String> findEnabledRoleCodes(List<String> roleCodes) {
        return RemoteResult.requireSuccess(
                () -> userHttpService.findByRoleCodeInAndStatus(roleCodes, NORMAL.getCode()))
            .stream()
            .map(role -> role.code())
            .toList();
    }

    @Override
    public void lockAfterPasswordFailures(Long userId) {
        RemoteResult.requireSuccess(() -> userHttpService.lockAfterPasswordFailures(userId));
    }

    @Override
    public void updateLoginTime(String username) {
        RemoteResult.requireSuccess(() -> userHttpService.updateLoginTime(username));
    }

    public List<RoleAuthorizationRpcVo> findAllRoleAuthorizations() {
        return RemoteResult.requireSuccess(userHttpService::findAllRoleAuthorizations);
    }

    public List<RoleAuthorizationRpcVo> findRoleAuthorizations(List<Long> roleIds) {
        return RemoteResult.requireSuccess(() -> userHttpService.findRoleAuthorizations(roleIds));
    }

    public List<MenuRpcVo> getCurrentUserNav(String rawRole) {
        return RemoteResult.requireSuccess(() -> menuHttpService.getCurrentUserNav(rawRole));
    }

    public List<AuthorityRpcVo> getSystemAuthorities() {
        return RemoteResult.requireSuccess(authorityHttpService::getAuthorities);
    }

    private static List<String> enabledCodes(List<RoleAuthorizationRpcVo> roles) {
        return roles.stream()
            .filter(RoleAuthorizationRpcVo::exists)
            .filter(role -> NORMAL.getCode().equals(role.status()))
            .map(RoleAuthorizationRpcVo::code)
            .toList();
    }
}
