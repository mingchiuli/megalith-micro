package wiki.chiu.micro.common.rpc;


import org.springframework.web.bind.annotation.RequestParam;
import wiki.chiu.micro.common.dto.AuthorityRpcDto;
import wiki.chiu.micro.common.dto.MenusAndButtonsRpcDto;
import wiki.chiu.micro.common.dto.RoleEntityRpcDto;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface UserHttpService {

    @GetExchange("/user/status")
    void changeUserStatusByUsername(@RequestParam String username, @RequestParam Integer status);

    @PostExchange("/user/role")
    Result<List<RoleEntityRpcDto>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @RequestParam Integer status);

    @PostExchange("/user/login/time")
    Result<Void> updateLoginTime(@RequestParam String username);

    @GetExchange("/user/email")
    Result<UserEntityRpcDto> findByEmail(@RequestParam String email);

    @GetExchange("/user/phone")
    Result<UserEntityRpcDto> findByPhone(@RequestParam String phone);

    @GetExchange("/user/role/{userId}")
    Result<List<String>> findRoleCodesByUserId(@PathVariable Long userId);

    @GetExchange("/user/{userId}")
    Result<UserEntityRpcDto> findById(@PathVariable Long userId);

    @GetExchange("/user/login/query")
    Result<UserEntityRpcDto> findByUsernameOrEmailOrPhone(@RequestParam String username);

    @GetExchange("/authority/role")
    Result<List<String>> getAuthoritiesByRoleCode(@RequestParam String rawRole);

    @GetExchange("/menu/nav")
    Result<MenusAndButtonsRpcDto> getCurrentUserNav(@RequestParam String role);

    @PostExchange("/authority/list")
    Result<List<AuthorityRpcDto>> getAuthorities(@RequestBody List<String> service);
    
    @GetExchange("/user/unlock")
    Result<Void> unlock();
  
}
