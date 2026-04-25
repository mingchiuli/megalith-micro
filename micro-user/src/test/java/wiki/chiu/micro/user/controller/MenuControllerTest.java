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
import wiki.chiu.micro.user.exception.GlobalExceptionHandler;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.service.MenuService;
import wiki.chiu.micro.user.vo.MenuEntityVo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @Mock
    private MenuAuthorityService menuAuthorityService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MenuController controller = new MenuController(menuService, menuAuthorityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void infoReturnsMenu() throws Exception {
        MenuEntityVo vo = MenuEntityVo.builder().id(1L).title("title").name("name").build();
        when(menuService.findById(1L)).thenReturn(vo);

        mockMvc.perform(get("/sys/menu/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("title"));
    }

    @Test
    void infoNotFoundReturns400() throws Exception {
        when(menuService.findById(anyLong())).thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/menu/info/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void listReturnsTree() throws Exception {
        when(menuService.tree()).thenReturn(List.of());

        mockMvc.perform(get("/sys/menu/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void downloadReturnsBytes() throws Exception {
        when(menuService.download()).thenReturn(new byte[]{0, 1});

        mockMvc.perform(get("/sys/menu/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{0, 1}));
    }

    @Test
    void saveAuthorityReturnsSuccess() throws Exception {
        mockMvc.perform(post("/sys/menu/authority/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getAuthoritiesInfoReturnsList() throws Exception {
        when(menuAuthorityService.getAuthoritiesInfo(any())).thenReturn(List.of());

        mockMvc.perform(get("/sys/menu/authority/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/menu/unknown"))
                .andExpect(status().isNotFound());
    }
}
