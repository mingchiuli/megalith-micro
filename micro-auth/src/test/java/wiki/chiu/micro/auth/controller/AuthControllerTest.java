package wiki.chiu.micro.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.auth.handler.AuthHttpHandler;
import wiki.chiu.micro.auth.handler.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.handler.CodeHttpHandler;
import wiki.chiu.micro.auth.handler.TokenHttpHandler;
import wiki.chiu.micro.auth.route.AuthRoutes;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.vo.AuthRpcVo;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @Mock private AuthHttpService authHttpService;

  @Mock private JwtTokenService jwtTokenService;

  @Mock private TokenHttpHandler tokenHttpHandler;

  @Mock private CodeHttpHandler codeHttpHandler;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"), List.of());
    lenient()
        .when(authHttpService.getAuthentication(anyString()))
        .thenReturn(
            Result.success(
                new AuthRpcVo(authInfo.userId(), authInfo.roles(), authInfo.authorities())));
    AuthHttpHandler handler = new AuthHttpHandler(authService);
    AuthInternalHttpHandler internalHandler =
        new AuthInternalHttpHandler(authService, jwtTokenService);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                AuthRoutes.routes(handler, tokenHttpHandler, codeHttpHandler, internalHandler))
            .build();
  }

  @Test
  void navReturnsMenuTree() throws Exception {
    MenuWithChildVo vo =
        MenuWithChildVo.builder()
            .id(1L)
            .name("backend")
            .children(
                List.of(
                    MenuWithChildVo.builder()
                        .id(2L)
                        .name("system-users-create")
                        .parentId(1L)
                        .type(2)
                        .children(List.of())
                        .build()))
            .build();
    when(authService.getCurrentUserNav(anyLong())).thenReturn(vo);

    mockMvc
        .perform(get("/auth/menu/nav").principal(() -> "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.name").value("backend"))
        .andExpect(jsonPath("$.data.children[0].name").value("system-users-create"));
  }

  @Test
  void navWhenServiceThrowsUnclassifiedBaseExceptionReturns500() throws Exception {
    when(authService.getCurrentUserNav(anyLong())).thenThrow(new BaseException("forbidden"));

    mockMvc
        .perform(get("/auth/menu/nav").principal(() -> "1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(9999))
        .andExpect(jsonPath("$.msg").value("forbidden"));
  }

  @Test
  void navUnknownPathReturns404() throws Exception {
    mockMvc.perform(get("/auth/menu/unknown")).andExpect(status().isNotFound());
  }

  @Test
  void internalRouteRejectsBlankMethodAndMapping() throws Exception {
    mockMvc
        .perform(
            post("/inner/auth/route")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\" \",\"routeMapping\":\"\"}"))
        .andExpect(status().isBadRequest());

    verify(authService, never()).findRoute(any());
  }
}
