package wiki.chiu.micro.common.rpc;


import org.springframework.web.bind.annotation.RequestParam;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface UserHttpService {

    @GetExchange("/user/status")
    Result<Void> changeUserStatusByUsername(@RequestParam String username, @RequestParam Integer status);

    @PostExchange("/user/role")
    Result<List<RoleEntityRpcVo>> findByRoleCodeInAndStatus(@RequestBody List<String> roles, @RequestParam Integer status);

    @PostExchange("/user/login/time")
    Result<Void> updateLoginTime(@RequestParam String username);

    @GetExchange("/user/email")
    Result<UserEntityRpcVo> findByEmail(@RequestParam String email);

    @GetExchange("/user/phone")
    Result<UserEntityRpcVo> findByPhone(@RequestParam String phone);

    @GetExchange("/user/role/{userId}")
    Result<List<String>> findRoleCodesByUserId(@PathVariable Long userId);

    @GetExchange("/user/{userId}")
    Result<UserEntityRpcVo> findById(@PathVariable Long userId);

    @GetExchange("/user/login/query")
    Result<UserEntityRpcVo> findByUsernameOrEmailOrPhone(@RequestParam String username);
    
    @GetExchange("/user/unlock")
    Result<Void> unlock();
  
}
