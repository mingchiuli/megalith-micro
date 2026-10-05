package wiki.chiu.micro.auth.adapter.in.http;

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

import wiki.chiu.micro.auth.application.model.MenuDisplay;
import wiki.chiu.micro.auth.application.port.in.AuthService;
import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenHttpHandler tokenHttpHandler;

    @Mock
    private CodeHttpHandler codeHttpHandler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthPrincipal authInfo = new AuthPrincipal(1L, List.of("ROLE_USER"));
        AuthHttpHandler handler = new AuthHttpHandler(authService);
        AuthInternalHttpHandler internalHandler =
            new AuthInternalHttpHandler(authService, tokenService);
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
        MenuDisplay child =
            new MenuDisplay(2L, 1L, null, "system-users-create", null, null, 2, null, 0, 0, List.of());
        MenuDisplay root =
            new MenuDisplay(1L, null, null, "backend", null, null, null, null, 0, 0, List.of(child));
        when(authService.getCurrentUserNav(anyList())).thenReturn(List.of(root));

        mockMvc
            .perform(get("/auth/menu/nav").principal(() -> "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("backend"))
            .andExpect(jsonPath("$.data.children[0].name").value("system-users-create"));
    }

    @Test
    void navWithoutRootMenuReturnsNullData() throws Exception {
        when(authService.getCurrentUserNav(anyList())).thenReturn(List.of());

        mockMvc
            .perform(get("/auth/menu/nav").principal(() -> "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist());
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
        when(tokenService.issueWebSocketTicket(1L, "blog-7")).thenReturn("ticket");

        mockMvc
            .perform(
                post("/inner/token/websocket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"roomId\":\"blog-7\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("Bearer ticket"));

        verify(tokenService).issueWebSocketTicket(1L, "blog-7");
    }

    @Test
    void internalWebSocketTicketRejectsMissingPrincipal() throws Exception {
        internalMockMvc()
            .perform(
                post("/inner/token/websocket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"roomId\":\"blog-7\"}"))
            .andExpect(status().isBadRequest());

        verify(tokenService, never()).issueWebSocketTicket(anyLong(), anyString());
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

        verify(tokenService, never()).issueWebSocketTicket(anyLong(), anyString());
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

        verify(tokenService, never()).issueWebSocketTicket(anyLong(), anyString());
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
                    new AuthInternalHttpHandler(authService, tokenService)))
            .build();
    }
}
