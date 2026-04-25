package wiki.chiu.micro.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.auth.exception.GlobalExceptionHandler;
import wiki.chiu.micro.auth.service.TokenService;
import wiki.chiu.micro.auth.support.StubAuthInfoResolver;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.MissException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenController tokenController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tokenController)
                .setCustomArgumentResolvers(new StubAuthInfoResolver(42L, List.of("ROLE_USER")))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void refreshTokenReturnsTokenMap() throws Exception {
        when(tokenService.refreshToken(42L)).thenReturn(Map.of("token", "newtoken"));

        mockMvc.perform(get("/token/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("newtoken"));
    }

    @Test
    void refreshTokenWhenServiceThrowsReturns400() throws Exception {
        when(tokenService.refreshToken(anyLong())).thenThrow(new MissException("token missing"));

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
