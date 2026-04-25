package wiki.chiu.micro.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.exception.GlobalExceptionHandler;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.vo.AuthorityVo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthorityControllerTest {

    @Mock
    private AuthorityService authorityService;

    @InjectMocks
    private AuthorityController authorityController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listReturnsList() throws Exception {
        when(authorityService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/sys/authority/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void infoReturnsAuthority() throws Exception {
        AuthorityVo vo = AuthorityVo.builder().id(1L).code("READ").build();
        when(authorityService.findById(1L)).thenReturn(vo);

        mockMvc.perform(get("/sys/authority/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("READ"));
    }

    @Test
    void infoNotFoundReturns400() throws Exception {
        when(authorityService.findById(anyLong())).thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/authority/info/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        doNothing().when(authorityService).deleteAuthorities(any());

        mockMvc.perform(post("/sys/authority/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void downloadReturnsBytes() throws Exception {
        when(authorityService.download()).thenReturn(new byte[]{9});

        mockMvc.perform(get("/sys/authority/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{9}));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/authority/unknown"))
                .andExpect(status().isNotFound());
    }
}
