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
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.support.StubAuthInfoResolver;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.exception.BaseException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setCustomArgumentResolvers(new StubAuthInfoResolver(1L, List.of("ROLE_USER")))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void navReturnsMenusAndButtons() throws Exception {
        MenusAndButtonsVo vo = MenusAndButtonsVo.builder()
                .menus(null)
                .buttons(List.of())
                .build();
        when(authService.getCurrentUserNav(anyList())).thenReturn(vo);

        mockMvc.perform(get("/auth/menu/nav"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void navWhenServiceThrowsBaseExceptionReturns400() throws Exception {
        when(authService.getCurrentUserNav(anyList())).thenThrow(new BaseException("forbidden"));

        mockMvc.perform(get("/auth/menu/nav"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("forbidden"));
    }

    @Test
    void navUnknownPathReturns404() throws Exception {
        mockMvc.perform(get("/auth/menu/unknown"))
                .andExpect(status().isNotFound());
    }
}
