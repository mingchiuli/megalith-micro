package org.chiu.micro.auth.rpc.wrapper;

import java.util.List;

import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.auth.dto.RoleEntityDto;
import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.exception.MissException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    public void changeUserStatusByUsername(String username, Integer status) {
        userHttpService.changeUserStatusByUsername(username, status);
    }

    public List<RoleEntityDto> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        Result<List<RoleEntityDto>> result = userHttpService.findByRoleCodeInAndStatus(roles, status);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public void updateLoginTime(String username) {
        userHttpService.updateLoginTime(username);
    }

    public void findByEmail(String loginEmail) {
        Result<UserEntityDto> result = userHttpService.findByEmail(loginEmail);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
    }

    public void findByPhone(String loginSMS) {
        Result<UserEntityDto> result = userHttpService.findByPhone(loginSMS);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        Result<List<String>> result = userHttpService.findRoleCodesByUserId(userId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public UserEntityDto findById(Long userId) {
        Result<UserEntityDto> result = userHttpService.findById(userId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public UserEntityDto findByUsernameOrEmailOrPhone(String username) {
        Result<UserEntityDto> result = userHttpService.findByUsernameOrEmailOrPhone(username);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public List<String> getAuthoritiesByRoleCode(String rawRole) {
        Result<List<String>> result = userHttpService.getAuthoritiesByRoleCode(rawRole);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public MenusAndButtonsRpcDto getCurrentUserNav(String rawRole) {
        Result<MenusAndButtonsRpcDto> result = userHttpService.getCurrentUserNav(rawRole);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}
