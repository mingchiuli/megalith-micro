package org.chiu.micro.gateway.server;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;


public interface AuthServer {

  @PostExchange("/login")
  byte[] login(@RequestParam String username, @RequestParam String password);

  @GetExchange("/code/email")
  byte[] createEmailCode(@RequestParam(value = "loginName") String loginEmail);

  @GetExchange("/code/sms")
  byte[] createSmsCode(@RequestParam(value = "loginName") String loginPhone);

  @GetExchange("/token/refresh")
  byte[] refreshToken(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @GetExchange("/token/userinfo")
  byte[] userinfo(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

  @GetExchange("/auth/menu/nav")
  byte[] nav(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);


}
