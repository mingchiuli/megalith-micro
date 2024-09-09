package org.chiu.micro.auth.rpc;

import java.util.List;

import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.auth.dto.RoleEntityDto;
import org.chiu.micro.auth.dto.UserEntityDto;
import org.chiu.micro.auth.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface UserHttpService {

    @GetExchange("/user/status/{username}/{status}")
    void changeUserStatusByUsername(@PathVariable String username, @PathVariable Integer status);

    @PostExchange("/user/role/{status}")
    Result<List<RoleEntityDto>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @PathVariable Integer status);

    @PostExchange("/user/login/time/{username}")
    void updateLoginTime(@PathVariable String username);

    @GetExchange("/user/email/{email}")
    Result<UserEntityDto> findByEmail(@PathVariable String email);

    @GetExchange("/user/phone/{phone}")
    Result<UserEntityDto> findByPhone(@PathVariable String phone);

    @GetExchange("/user/role/{userId}")
    Result<List<String>> findRoleCodesByUserId(@PathVariable Long userId);

    @GetExchange("/user/{userId}")
    Result<UserEntityDto> findById(@PathVariable Long userId);

    @GetExchange("/user/login/query/{username}")
    Result<UserEntityDto> findByUsernameOrEmailOrPhone(@PathVariable String username);

    @GetExchange("/user/authority/role/{rawRole}")
    Result<List<String>> getAuthoritiesByRoleCode(@PathVariable String rawRole);

    @GetExchange("/menu/nav/{role}")
    Result<MenusAndButtonsRpcDto> getCurrentUserNav(@PathVariable String role);

    @PostExchange("/authority/list")
    Result<List<AuthorityDto>> getAuthorities(@RequestBody List<String> service);
  
}
