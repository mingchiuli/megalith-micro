package wiki.chiu.micro.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
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

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.adapter.in.http.AuthorityHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.MenuHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.MenuInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.RoleHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserInternalHttpHandler;
import wiki.chiu.micro.user.adapter.in.http.UserRoutes;
import wiki.chiu.micro.user.application.port.in.RoleDataPermissionService;
import wiki.chiu.micro.user.application.port.in.RoleMenuService;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityVo;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private RoleMenuService roleMenuService;

    @Mock
    private RoleDataPermissionService roleDataPermissionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ValidatedRequest validation = new ValidatedRequest();
        RoleHttpHandler handler =
            new RoleHttpHandler(roleService, roleMenuService, roleDataPermissionService, validation);
        mockMvc =
            MockMvcBuilders.routerFunctions(
                    UserRoutes.routes(
                        org.mockito.Mockito.mock(UserHttpHandler.class),
                        handler,
                        org.mockito.Mockito.mock(MenuHttpHandler.class),
                        org.mockito.Mockito.mock(AuthorityHttpHandler.class),
                        org.mockito.Mockito.mock(UserInternalHttpHandler.class),
                        org.mockito.Mockito.mock(MenuInternalHttpHandler.class),
                        org.mockito.Mockito.mock(AuthorityInternalHttpHandler.class)))
                .build();
    }

    @Test
    void infoReturnsRole() throws Exception {
        RoleEntityVo vo = RoleEntityVo.builder().id(1L).name("admin").code("ADMIN").build();
        when(roleService.info(1L)).thenReturn(vo);

        mockMvc
            .perform(get("/sys/role/info/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("admin"));
    }

    @Test
    void infoNotFoundReturns404() throws Exception {
        when(roleService.info(anyLong())).thenThrow(new MissException("not found"));

        mockMvc
            .perform(get("/sys/role/info/9"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void getPageReturnsPage() throws Exception {
        when(roleService.getPage(anyInt(), anyInt())).thenReturn(PageAdapter.emptyPage());

        mockMvc
            .perform(get("/sys/role/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        doNothing().when(roleService).delete(any());

        mockMvc
            .perform(post("/sys/role/delete").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void invalidStatusIsRejectedBeforeHandler() throws Exception {
        mockMvc
            .perform(
                post("/sys/role/save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"id\":null,\"name\":\"admin\",\"code\":\"ADMIN\","
                            + "\"remark\":\"\",\"status\":9}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("status must be between 0 and 1"));

        verify(roleService, never()).saveOrUpdate(any());
    }

    @Test
    void saveRoleReturnsSuccess() throws Exception {
        mockMvc
            .perform(
                post("/sys/role/save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"id\":null,\"name\":\"editor\",\"code\":\"editor\","
                            + "\"remark\":\"edit blogs\",\"status\":0}"))
            .andExpect(status().isOk());

        verify(roleService).saveOrUpdate(any());
    }

    @Test
    void saveMenuReturnsSuccess() throws Exception {
        doNothing().when(roleMenuService).saveMenu(anyLong(), any());

        mockMvc
            .perform(
                post("/sys/role/menu/3").contentType(MediaType.APPLICATION_JSON).content("[10,11]"))
            .andExpect(status().isOk());
    }

    @Test
    void saveMenuRejectsNonPositiveId() throws Exception {
        mockMvc
            .perform(post("/sys/role/menu/3").contentType(MediaType.APPLICATION_JSON).content("[0]"))
            .andExpect(status().isBadRequest());

        verify(roleMenuService, never()).saveMenu(anyLong(), any());
    }

    @Test
    void getMenusInfoReturnsList() throws Exception {
        when(roleMenuService.getMenusInfo(3L)).thenReturn(List.of());

        mockMvc
            .perform(get("/sys/role/menu/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void saveDataPermissionsReturnsSuccess() throws Exception {
        mockMvc
            .perform(
                post("/sys/role/data-permission/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[\"BLOG_VIEW_ALL\",\"BLOG_EDIT_ALL\"]"))
            .andExpect(status().isOk());

        verify(roleDataPermissionService)
            .saveDataPermissions(
                3L, List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_EDIT_ALL));
    }

    @Test
    void saveDataPermissionsAcceptsEmptyList() throws Exception {
        mockMvc
            .perform(
                post("/sys/role/data-permission/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[]"))
            .andExpect(status().isOk());

        verify(roleDataPermissionService).saveDataPermissions(3L, List.of());
    }

    @Test
    void saveDataPermissionsRejectsNullElements() throws Exception {
        mockMvc
            .perform(
                post("/sys/role/data-permission/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[null]"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("dataPermissions must not contain null"));

        verify(roleDataPermissionService, never()).saveDataPermissions(anyLong(), any());
    }

    @Test
    void getDataPermissionsReturnsList() throws Exception {
        when(roleDataPermissionService.getDataPermissions(3L))
            .thenReturn(List.of(DataPermissionEnum.BLOG_VIEW_ALL));

        mockMvc
            .perform(get("/sys/role/data-permission/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0]").value("BLOG_VIEW_ALL"));
    }

    @Test
    void downloadReturnsBytes() throws Exception {
        when(roleService.download()).thenReturn(new byte[]{1, 2, 3});

        mockMvc
            .perform(get("/sys/role/download"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void getValidAllReturnsList() throws Exception {
        when(roleService.getValidAll()).thenReturn(List.of());

        mockMvc
            .perform(get("/sys/role/valid/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/role/unknown")).andExpect(status().isNotFound());
    }
}
