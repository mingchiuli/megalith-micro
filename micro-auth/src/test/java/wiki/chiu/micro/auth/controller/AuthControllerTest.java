package wiki.chiu.micro.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @Mock private JwtTokenService jwtTokenService;

  @Mock private TokenHttpHandler tokenHttpHandler;

  @Mock private CodeHttpHandler codeHttpHandler;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"));
    AuthHttpHandler handler = new AuthHttpHandler(authService);
    AuthInternalHttpHandler internalHandler =
        new AuthInternalHttpHandler(authService, jwtTokenService);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                AuthRoutes.routes(handler, tokenHttpHandler, codeHttpHandler, internalHandler))
            .defaultRequest(
                get("/")
                    .header(AuthPrincipalCodec.HEADER_NAME, AuthPrincipalCodec.encode(authInfo)))
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
    when(authService.getCurrentUserNav(anyList())).thenReturn(vo);

    mockMvc
        .perform(get("/auth/menu/nav").principal(() -> "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.name").value("backend"))
        .andExpect(jsonPath("$.data.children[0].name").value("system-users-create"));
  }

  @Test
  void navWhenServiceThrowsUnclassifiedBaseExceptionReturns500() throws Exception {
    when(authService.getCurrentUserNav(anyList())).thenThrow(new BaseException("forbidden"));

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

    verify(authService, never()).authorizeRoute(any(), anyString());
  }

  @Test
  void internalRouteReturnsUnauthorizedForInvalidToken() throws Exception {
    when(authService.authorizeRoute(any(), anyString()))
        .thenThrow(new MissException(ExceptionMessage.TOKEN_INVALID));

    mockMvc
        .perform(
            post("/inner/auth/route")
                .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"method\":\"GET\",\"routeMapping\":\"/api/private\",\"ipAddr\":\"unknown\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(ExceptionMessage.TOKEN_INVALID.getCode()));
  }

  @Test
  void internalWebSocketTicketUsesThePrincipalHeader() throws Exception {
    when(jwtTokenService.issueWebSocketToken(1L, "blog-7")).thenReturn("ticket");

    mockMvc
        .perform(
            post("/inner/token/websocket")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"blog-7\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("Bearer ticket"));

    verify(jwtTokenService).issueWebSocketToken(1L, "blog-7");
  }

  @Test
  void internalWebSocketTicketRejectsMissingPrincipal() throws Exception {
    internalMockMvc()
        .perform(
            post("/inner/token/websocket")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"blog-7\"}"))
        .andExpect(status().isBadRequest());

    verify(jwtTokenService, never()).issueWebSocketToken(anyLong(), anyString());
  }

  @Test
  void internalWebSocketTicketRejectsInvalidPrincipal() throws Exception {
    internalMockMvc()
        .perform(
            post("/inner/token/websocket")
                .header(InternalHttpHeaders.PRINCIPAL, "not-base64")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"blog-7\"}"))
        .andExpect(status().isBadRequest());

    verify(jwtTokenService, never()).issueWebSocketToken(anyLong(), anyString());
  }

  @Test
  void internalWebSocketTicketRejectsBlankPrincipal() throws Exception {
    internalMockMvc()
        .perform(
            post("/inner/token/websocket")
                .header(InternalHttpHeaders.PRINCIPAL, " ")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\":\"blog-7\"}"))
        .andExpect(status().isBadRequest());

    verify(jwtTokenService, never()).issueWebSocketToken(anyLong(), anyString());
  }

  @Test
  void internalWebSocketTicketRejectsTheWrongHttpMethod() throws Exception {
    mockMvc.perform(get("/inner/token/websocket")).andExpect(status().isNotFound());
  }

  private MockMvc internalMockMvc() {
    return MockMvcBuilders.routerFunctions(
            AuthRoutes.routes(
                new AuthHttpHandler(authService),
                tokenHttpHandler,
                codeHttpHandler,
                new AuthInternalHttpHandler(authService, jwtTokenService)))
        .build();
  }
}
