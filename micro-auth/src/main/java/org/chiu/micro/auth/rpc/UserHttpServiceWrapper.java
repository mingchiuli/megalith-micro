package org.chiu.micro.auth.rpc;

import org.chiu.micro.common.dto.AuthorityRpcDto;
import org.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.common.dto.RoleEntityRpcDto;
import org.chiu.micro.common.dto.UserEntityRpcDto;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.common.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import java.util.List;

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
        Result<List<RoleEntityRpcDto>> result = userHttpService.findByRoleCodeInAndStatus(roles, status);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public void updateLoginTime(String username) {
        Result<Void> result = userHttpService.updateLoginTime(username);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

    public void unlock() {
        Result<Void> result = userHttpService.unlock();
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

    public void findByEmail(String loginEmail) {
        Result<UserEntityRpcDto> result = userHttpService.findByEmail(loginEmail);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

    public void findByPhone(String loginSMS) {
        Result<UserEntityRpcDto> result = userHttpService.findByPhone(loginSMS);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        Result<List<String>> result = userHttpService.findRoleCodesByUserId(userId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public UserEntityRpcDto findById(Long userId) {
        Result<UserEntityRpcDto> result = userHttpService.findById(userId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public UserEntityRpcDto findByUsernameOrEmailOrPhone(String username) {
        Result<UserEntityRpcDto> result = userHttpService.findByUsernameOrEmailOrPhone(username);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<String> getAuthoritiesByRoleCode(String rawRole) {
        Result<List<String>> result = userHttpService.getAuthoritiesByRoleCode(rawRole);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public MenusAndButtonsRpcDto getCurrentUserNav(String rawRole) {
        Result<MenusAndButtonsRpcDto> result = userHttpService.getCurrentUserNav(rawRole);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<AuthorityRpcDto> getSystemAuthorities(List<String> serviceHost) {
        Result<List<AuthorityRpcDto>> result = userHttpService.getAuthorities(serviceHost);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}
