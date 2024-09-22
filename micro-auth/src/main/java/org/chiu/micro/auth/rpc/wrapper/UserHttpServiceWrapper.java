package org.chiu.micro.auth.rpc.wrapper;

import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.auth.dto.RoleEntityDto;
import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.exception.MissException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.rpc.UserHttpService;
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

    public List<RoleEntityDto> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        Result<List<RoleEntityDto>> result = userHttpService.findByRoleCodeInAndStatus(roles, status);
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
        Result<UserEntityDto> result = userHttpService.findByEmail(loginEmail);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
    }

    public void findByPhone(String loginSMS) {
        Result<UserEntityDto> result = userHttpService.findByPhone(loginSMS);
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

    public UserEntityDto findById(Long userId) {
        Result<UserEntityDto> result = userHttpService.findById(userId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public UserEntityDto findByUsernameOrEmailOrPhone(String username) {
        Result<UserEntityDto> result = userHttpService.findByUsernameOrEmailOrPhone(username);
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

    public List<AuthorityDto> getSystemAuthorities(List<String> serviceHost) {
        Result<List<AuthorityDto>> result = userHttpService.getAuthorities(serviceHost);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}
