package wiki.chiu.micro.auth.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.auth.handler.AuthHttpHandler;
import wiki.chiu.micro.auth.handler.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.handler.CodeHttpHandler;
import wiki.chiu.micro.auth.handler.TokenHttpHandler;
import wiki.chiu.micro.auth.route.AuthRoutes;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.token.AccessTokenCookieManager;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.auth.token.TokenCookieProperties;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.MissException;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

  @Mock private TokenService tokenService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    TokenCookieProperties cookieProperties = new TokenCookieProperties("/", true, "Strict");
    JwtProperties jwtProperties =
        new JwtProperties(
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
            900,
            604800,
            300,
            "micro-auth",
            "megalith-api");
    RefreshTokenCookieManager cookieManager =
        new RefreshTokenCookieManager(cookieProperties, jwtProperties);
    AccessTokenCookieManager accessCookieManager =
        new AccessTokenCookieManager(cookieProperties, jwtProperties);
    TokenHttpHandler handler =
        new TokenHttpHandler(tokenService, cookieManager, accessCookieManager);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                AuthRoutes.routes(
                    org.mockito.Mockito.mock(AuthHttpHandler.class),
                    handler,
                    org.mockito.Mockito.mock(CodeHttpHandler.class),
                    org.mockito.Mockito.mock(AuthInternalHttpHandler.class)))
            .build();
  }

  @Test
  void refreshTokenReturnsTokenMap() throws Exception {
    when(tokenService.refreshToken(42L)).thenReturn(Map.of("accessToken", "newtoken"));

    mockMvc
        .perform(post("/token/refresh").principal(() -> "42"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.accessToken").value("newtoken"))
        .andExpect(
            header()
                .string(
                    "Set-Cookie",
                    org.hamcrest.Matchers.containsString("megalith_access_token=newtoken")));
  }

  @Test
  void refreshTokenWhenServiceCannotFindTokenReturns404() throws Exception {
    when(tokenService.refreshToken(anyLong())).thenThrow(new MissException("token missing"));

    mockMvc
        .perform(post("/token/refresh").principal(() -> "42"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value("token missing"));
  }

  @Test
  void userinfoReturnsUserInfoVo() throws Exception {
    UserInfoVo vo = UserInfoVo.builder().id(42L).nickname("nick").avatar("avatar.png").build();
    when(tokenService.userinfo(42L)).thenReturn(vo);

    mockMvc
        .perform(get("/token/userinfo").principal(() -> "42"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(42))
        .andExpect(jsonPath("$.data.nickname").value("nick"))
        .andExpect(jsonPath("$.data.avatar").value("avatar.png"));
  }

  @Test
  void logoutExpiresTokenCookies() throws Exception {
    mockMvc
        .perform(post("/token/logout"))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              var cookies = result.getResponse().getHeaders("Set-Cookie");
              org.junit.jupiter.api.Assertions.assertTrue(
                  cookies.stream().anyMatch(value -> value.contains("megalith_access_token=")));
              org.junit.jupiter.api.Assertions.assertTrue(
                  cookies.stream().anyMatch(value -> value.contains("megalith_refresh_token=")));
              org.junit.jupiter.api.Assertions.assertTrue(
                  cookies.stream().allMatch(value -> value.contains("Max-Age=0")));
            })
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void unknownTokenPathReturns404() throws Exception {
    mockMvc.perform(get("/token/unknown")).andExpect(status().isNotFound());
  }
}
