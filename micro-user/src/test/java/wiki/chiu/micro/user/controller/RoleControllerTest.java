package wiki.chiu.micro.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.exception.GlobalExceptionHandler;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityVo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private RoleMenuService roleMenuService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RoleController controller = new RoleController(roleService, roleMenuService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void infoReturnsRole() throws Exception {
        RoleEntityVo vo = RoleEntityVo.builder().id(1L).name("admin").code("ADMIN").build();
        when(roleService.info(1L)).thenReturn(vo);

        mockMvc.perform(get("/sys/role/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("admin"));
    }

    @Test
    void infoNotFoundReturns400() throws Exception {
        when(roleService.info(anyLong())).thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/role/info/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void getPageReturnsPage() throws Exception {
        when(roleService.getPage(anyInt(), anyInt())).thenReturn(PageAdapter.emptyPage());

        mockMvc.perform(get("/sys/role/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        doNothing().when(roleService).delete(any());

        mockMvc.perform(post("/sys/role/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void saveMenuReturnsSuccess() throws Exception {
        doNothing().when(roleMenuService).saveMenu(anyLong(), any());

        mockMvc.perform(post("/sys/role/menu/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[10,11]"))
                .andExpect(status().isOk());
    }

    @Test
    void getMenusInfoReturnsList() throws Exception {
        when(roleMenuService.getMenusInfo(3L)).thenReturn(List.of());

        mockMvc.perform(get("/sys/role/menu/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void downloadReturnsBytes() throws Exception {
        when(roleService.download()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/sys/role/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void getValidAllReturnsList() throws Exception {
        when(roleService.getValidAll()).thenReturn(List.of());

        mockMvc.perform(get("/sys/role/valid/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/role/unknown"))
                .andExpect(status().isNotFound());
    }
}
