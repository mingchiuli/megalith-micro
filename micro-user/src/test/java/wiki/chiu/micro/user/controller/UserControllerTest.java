package wiki.chiu.micro.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.exception.GlobalExceptionHandler;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRoleService userRoleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService, userRoleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRegisterPageReturnsToken() throws Exception {
        when(userService.getRegisterPage("alice")).thenReturn("token-x");

        mockMvc.perform(get("/sys/user/auth/register/page").param("username", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("token-x"));
    }

    @Test
    void getRegisterPageMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/sys/user/auth/register/page"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkRegisterPageReturnsBoolean() throws Exception {
        when(userService.checkRegisterPage("tk")).thenReturn(true);

        mockMvc.perform(get("/sys/user/register/check").param("token", "tk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void imageUploadReturnsUrl() throws Exception {
        when(userService.imageUpload(anyString(), any())).thenReturn("https://oss/x.png");
        MockMultipartFile file = new MockMultipartFile("image", "x.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/sys/user/register/image/upload")
                        .file(file).param("token", "tk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://oss/x.png"));
    }

    @Test
    void imageDeleteReturnsSuccess() throws Exception {
        doNothing().when(userService).imageDelete("tk", "https://oss/x.png");

        mockMvc.perform(get("/sys/user/register/image/delete")
                        .param("url", "https://oss/x.png").param("token", "tk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        doNothing().when(userService).deleteUsers(any());

        mockMvc.perform(post("/sys/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isOk());
    }

    @Test
    void pageReturnsPage() throws Exception {
        when(userService.listPage(anyInt(), anyInt())).thenReturn(PageAdapter.emptyPage());

        mockMvc.perform(get("/sys/user/page/1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    void infoNotFoundReturns400() throws Exception {
        when(userService.findInfo(anyLong())).thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/user/info/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/user/unknown"))
                .andExpect(status().isNotFound());
    }
}
