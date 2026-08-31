package wiki.chiu.micro.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.adapter.in.http.AuthorityHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.MenuHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.MenuInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.RoleHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserRoutes;
import wiki.chiu.micro.user.application.port.in.MenuAuthorityService;
import wiki.chiu.micro.user.application.port.in.MenuService;
import wiki.chiu.micro.user.vo.MenuEntityVo;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @Mock
    private MenuAuthorityService menuAuthorityService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ValidatedRequest validation = new ValidatedRequest();
        MenuHttpHandler handler = new MenuHttpHandler(menuService, menuAuthorityService, validation);
        mockMvc =
            MockMvcBuilders.routerFunctions(
                    UserRoutes.routes(
                        org.mockito.Mockito.mock(UserHttpHandler.class),
                        org.mockito.Mockito.mock(RoleHttpHandler.class),
                        handler,
                        org.mockito.Mockito.mock(AuthorityHttpHandler.class),
                        org.mockito.Mockito.mock(UserInternalHttpHandler.class),
                        org.mockito.Mockito.mock(MenuInternalHttpHandler.class),
                        org.mockito.Mockito.mock(AuthorityInternalHttpHandler.class)))
                .build();
    }

    @Test
    void infoReturnsMenu() throws Exception {
        MenuEntityVo vo = MenuEntityVo.builder().id(1L).title("title").name("name").build();
        when(menuService.findById(1L)).thenReturn(vo);

        mockMvc
            .perform(get("/sys/menu/info/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("title"));
    }

    @Test
    void infoNotFoundReturns404() throws Exception {
        when(menuService.findById(anyLong())).thenThrow(new MissException("not found"));

        mockMvc
            .perform(get("/sys/menu/info/9"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void listReturnsTree() throws Exception {
        when(menuService.tree()).thenReturn(List.of());

        mockMvc
            .perform(get("/sys/menu/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void downloadReturnsBytes() throws Exception {
        when(menuService.download()).thenReturn(new byte[]{0, 1});

        mockMvc
            .perform(get("/sys/menu/download"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(new byte[]{0, 1}));
    }

    @Test
    void saveAuthorityReturnsSuccess() throws Exception {
        mockMvc
            .perform(
                post("/sys/menu/authority/3").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getAuthoritiesInfoReturnsList() throws Exception {
        when(menuAuthorityService.getAuthoritiesInfo(any())).thenReturn(List.of());

        mockMvc
            .perform(get("/sys/menu/authority/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void deletingParentBeforeChildrenReturns409() throws Exception {
        doThrow(new BaseException(ExceptionMessage.MENU_INVALID_OPERATE)).when(menuService).delete(3L);

        mockMvc
            .perform(post("/sys/menu/delete/3"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(45));

        verify(menuService).delete(3L);
    }

    @Test
    void invalidMenuBodyIsRejectedBeforeHandler() throws Exception {
        mockMvc
            .perform(post("/sys/menu/save").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("parentId must not be null"));

        verify(menuService, never()).saveOrUpdate(any());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/menu/unknown")).andExpect(status().isNotFound());
    }
}
