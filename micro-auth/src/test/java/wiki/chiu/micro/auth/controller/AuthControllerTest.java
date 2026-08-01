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
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.web.ValidatedRequest;

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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthHttpHandler handler = new AuthHttpHandler(authService);
        AuthInfo authInfo = new AuthInfo(1L, List.of("ROLE_USER"), List.of());
        mockMvc = MockMvcBuilders.routerFunctions(AuthRoutes.routes(
                        handler,
                        org.mockito.Mockito.mock(TokenHttpHandler.class),
                        org.mockito.Mockito.mock(CodeHttpHandler.class),
                        org.mockito.Mockito.mock(AuthInternalHttpHandler.class),
                        request -> authInfo,
                        new ValidatedRequest(jakarta.validation.Validation
                                .buildDefaultValidatorFactory().getValidator())))
                .build();
    }

    @Test
    void navReturnsMenuTree() throws Exception {
        MenuWithChildVo vo = MenuWithChildVo.builder()
                .id(1L)
                .name("backend")
                .children(List.of(MenuWithChildVo.builder()
                        .id(2L)
                        .name("system-users-create")
                        .parentId(1L)
                        .type(2)
                        .children(List.of())
                        .build()))
                .build();
        when(authService.getCurrentUserNav(anyList())).thenReturn(vo);

        mockMvc.perform(get("/auth/menu/nav"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("backend"))
                .andExpect(jsonPath("$.data.children[0].name").value("system-users-create"));
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
