package wiki.chiu.micro.auth.rpc;

import wiki.chiu.micro.common.dto.AuthorityRpcDto;
import wiki.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import wiki.chiu.micro.common.dto.RoleEntityRpcDto;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserHttpServiceWrapper {

    private static final int SUCCESS_CODE = 200;

    private final UserHttpService userHttpService;

    public UserHttpServiceWrapper(UserHttpService userHttpService) {
        this.userHttpService = userHttpService;
    }

    public void changeUserStatusByUsername(String username, Integer status) {
        userHttpService.changeUserStatusByUsername(username, status);
    }

    public List<RoleEntityRpcDto> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        return handleResult(userHttpService.findByRoleCodeInAndStatus(roles, status));
    }

    public void updateLoginTime(String username) {
        handleResult(userHttpService.updateLoginTime(username));
    }

    public void unlock() {
        handleResult(userHttpService.unlock());
    }

    public void findByEmail(String loginEmail) {
        handleResult(userHttpService.findByEmail(loginEmail));
    }

    public void findByPhone(String loginSMS) {
        handleResult(userHttpService.findByPhone(loginSMS));
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        return handleResult(userHttpService.findRoleCodesByUserId(userId));
    }

    public UserEntityRpcDto findById(Long userId) {
        return handleResult(userHttpService.findById(userId));
    }

    public UserEntityRpcDto findByUsernameOrEmailOrPhone(String username) {
        return handleResult(userHttpService.findByUsernameOrEmailOrPhone(username));
    }

    public Set<String> getAuthoritiesByRoleCode(String rawRole) {
        return handleResult(userHttpService.getAuthoritiesByRoleCode(rawRole));
    }

    public MenusAndButtonsRpcDto getCurrentUserNav(String rawRole) {
        return handleResult(userHttpService.getCurrentUserNav(rawRole));
    }

    public List<AuthorityRpcDto> getSystemAuthorities(List<String> serviceHost) {
        return handleResult(userHttpService.getAuthorities(serviceHost));
    }

    private <T> T handleResult(Result<T> result) {
        if (result.code() != SUCCESS_CODE) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}
