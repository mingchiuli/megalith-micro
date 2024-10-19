package org.chiu.micro.common.rpc;


import org.chiu.micro.common.dto.AuthorityRpcDto;
import org.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import org.chiu.micro.common.dto.RoleEntityRpcDto;
import org.chiu.micro.common.dto.UserEntityRpcDto;
import org.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface UserHttpService {

    @GetExchange("/user/status/{username}/{status}")
    void changeUserStatusByUsername(@PathVariable String username, @PathVariable Integer status);

    @PostExchange("/user/role/{status}")
    Result<List<RoleEntityRpcDto>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @PathVariable Integer status);

    @PostExchange("/user/login/time/{username}")
    Result<Void> updateLoginTime(@PathVariable String username);

    @GetExchange("/user/email/{email}")
    Result<UserEntityRpcDto> findByEmail(@PathVariable String email);

    @GetExchange("/user/phone/{phone}")
    Result<UserEntityRpcDto> findByPhone(@PathVariable String phone);

    @GetExchange("/user/role/{userId}")
    Result<List<String>> findRoleCodesByUserId(@PathVariable Long userId);

    @GetExchange("/user/{userId}")
    Result<UserEntityRpcDto> findById(@PathVariable Long userId);

    @GetExchange("/user/login/query/{username}")
    Result<UserEntityRpcDto> findByUsernameOrEmailOrPhone(@PathVariable String username);

    @GetExchange("/authority/role/{rawRole}")
    Result<List<String>> getAuthoritiesByRoleCode(@PathVariable String rawRole);

    @GetExchange("/menu/nav/{role}")
    Result<MenusAndButtonsRpcDto> getCurrentUserNav(@PathVariable String role);

    @PostExchange("/authority/list")
    Result<List<AuthorityRpcDto>> getAuthorities(@RequestBody List<String> service);
    
    @GetExchange("/user/unlock")
    Result<Void> unlock();
  
}
