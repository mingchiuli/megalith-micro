package org.chiu.micro.gateway.server.wrapper;


import org.chiu.micro.gateway.server.AuthServer;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping
public class AuthServerWrapper {

  private final AuthServer authServer;

  @PostMapping("/login")
  public byte[] login(@RequestParam String username, @RequestParam String password) {
    return authServer.login(username, password);
  }

  @GetMapping("/code/email")
  public byte[] createEmailCode(@RequestParam(value = "loginName") String loginEmail) {
    return authServer.createEmailCode(loginEmail);
  }

  @GetMapping("/code/sms")
  public byte[] createSmsCode(@RequestParam(value = "loginName") String loginPhone) {
    return authServer.createSmsCode(loginPhone);
  }

  @GetMapping("/token/refresh")
  public byte[] refreshToken(HttpServletRequest request) {
    return authServer.refreshToken(request.getHeader(HttpHeaders.AUTHORIZATION));
  }

  @GetMapping("/token/userinfo")
  public byte[] userinfo(HttpServletRequest request) {
    return authServer.userinfo(request.getHeader(HttpHeaders.AUTHORIZATION));
  }

  @GetMapping("/auth/menu/nav")
  public byte[] nav(HttpServletRequest request) {
    return authServer.nav(request.getHeader(HttpHeaders.AUTHORIZATION));
  }

}