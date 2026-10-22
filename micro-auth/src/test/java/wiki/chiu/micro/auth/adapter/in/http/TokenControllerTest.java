package wiki.chiu.micro.auth.adapter.in.http;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import wiki.chiu.micro.auth.application.model.UserInfo;
import wiki.chiu.micro.auth.application.port.in.TokenService;
import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.security.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TokenHttpHandler handler =
            new TokenHttpHandler(tokenService, new SessionCookies("/", true, "Strict", 900, 604800));
        mockMvc =
            MockMvcBuilders.routerFunctions(
                    AuthRoutes.routes(
                        mock(AuthHttpHandler.class),
                        handler,
                        mock(CodeHttpHandler.class),
                        mock(AuthInternalHttpHandler.class)))
                .defaultRequest(
                    get("/")
                        .header(
                            AuthPrincipalCodec.HEADER_NAME,
                            AuthPrincipalCodec.encode(new AuthPrincipal(42L, List.of("ROLE_USER")))))
                .build();
    }

    @Test
    void refreshTokenReturnsAccessCookieWithoutTokenBody() throws Exception {
        when(tokenService.refreshAccessToken(42L)).thenReturn("newtoken");

        mockMvc
            .perform(post("/token/refresh").principal(() -> "42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(nullValue()))
            .andExpect(
                content()
                    .string(
                        not(containsString("newtoken"))))
            .andExpect(
                header()
                    .string(
                        "Set-Cookie",
                        containsString("megalith_access_token=newtoken")));
    }

    @Test
    void refreshTokenWhenServiceCannotFindTokenReturns404() throws Exception {
        when(tokenService.refreshAccessToken(anyLong())).thenThrow(new MissException("token missing"));

        mockMvc
            .perform(post("/token/refresh").principal(() -> "42"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.msg").value("token missing"));
    }

    @Test
    void userinfoReturnsUserInfoVo() throws Exception {
        when(tokenService.userinfo(42L)).thenReturn(new UserInfo(42L, "nick", "avatar.png"));

        mockMvc
            .perform(get("/token/userinfo"))
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
                    assertTrue(
                        cookies.stream().anyMatch(value -> value.contains("megalith_access_token=")));
                    assertTrue(
                        cookies.stream().anyMatch(value -> value.contains("megalith_refresh_token=")));
                    assertTrue(
                        cookies.stream().allMatch(value -> value.contains("Max-Age=0")));
                })
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void unknownTokenPathReturns404() throws Exception {
        mockMvc.perform(get("/token/unknown")).andExpect(status().isNotFound());
    }
}
