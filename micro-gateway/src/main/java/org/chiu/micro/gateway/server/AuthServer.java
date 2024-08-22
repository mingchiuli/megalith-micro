package org.chiu.micro.gateway.server;

import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.vo.LoginSuccessVo;
import org.chiu.micro.gateway.vo.MenusAndButtonsVo;
import org.chiu.micro.gateway.vo.UserInfoVo;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

public interface AuthServer {

  @PostExchange("/login")
  Result<LoginSuccessVo> login(@RequestParam String username, @RequestParam String password);

  @GetExchange("/code/email")
  Result<Void> createEmailCode(@RequestParam(value = "loginName") String loginEmail);

  @GetExchange("/code/sms")
  Result<Void> createSmsCode(@RequestParam(value = "loginName") String loginPhone);

  @GetExchange("/token/refresh")
  Result<Map<String, String>> refreshToken(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @GetExchange("/token/userinfo")
  Result<UserInfoVo> userinfo(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @GetExchange("/auth/menu/nav")
  Result<MenusAndButtonsVo> nav(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);


}
