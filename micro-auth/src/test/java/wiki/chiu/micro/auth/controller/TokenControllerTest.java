package wiki.chiu.micro.auth.controller;

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
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static wiki.chiu.micro.common.lang.Const.REFRESH;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TokenHttpHandler handler = new TokenHttpHandler(tokenService);
        AuthInfo authInfo = new AuthInfo(42L, List.of(REFRESH), List.of());
        mockMvc = MockMvcBuilders.routerFunctions(AuthRoutes.routes(
                        org.mockito.Mockito.mock(AuthHttpHandler.class),
                        handler,
                        org.mockito.Mockito.mock(CodeHttpHandler.class),
                        org.mockito.Mockito.mock(AuthInternalHttpHandler.class),
                        request -> authInfo,
                        new ValidatedRequest(jakarta.validation.Validation
                                .buildDefaultValidatorFactory().getValidator())))
                .build();
    }

    @Test
    void refreshTokenReturnsTokenMap() throws Exception {
        when(tokenService.refreshToken(42L, List.of(REFRESH))).thenReturn(Map.of("token", "newtoken"));

        mockMvc.perform(get("/token/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("newtoken"));
    }

    @Test
    void refreshTokenWhenServiceThrowsReturns400() throws Exception {
        when(tokenService.refreshToken(anyLong(), anyList())).thenThrow(new MissException("token missing"));

        mockMvc.perform(get("/token/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("token missing"));
    }

    @Test
    void userinfoReturnsUserInfoVo() throws Exception {
        UserInfoVo vo = UserInfoVo.builder()
                .id(42L)
                .nickname("nick")
                .avatar("avatar.png")
                .build();
        when(tokenService.userinfo(42L)).thenReturn(vo);

        mockMvc.perform(get("/token/userinfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(42))
                .andExpect(jsonPath("$.data.nickname").value("nick"))
                .andExpect(jsonPath("$.data.avatar").value("avatar.png"));
    }

    @Test
    void unknownTokenPathReturns404() throws Exception {
        mockMvc.perform(get("/token/unknown"))
                .andExpect(status().isNotFound());
    }
}
