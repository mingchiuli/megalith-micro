package wiki.chiu.micro.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.handler.AuthorityHttpHandler;
import wiki.chiu.micro.user.handler.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.handler.MenuHttpHandler;
import wiki.chiu.micro.user.handler.MenuInternalHttpHandler;
import wiki.chiu.micro.user.handler.RoleHttpHandler;
import wiki.chiu.micro.user.handler.UserHttpHandler;
import wiki.chiu.micro.user.handler.UserInternalHttpHandler;
import wiki.chiu.micro.user.route.UserRoutes;
import wiki.chiu.micro.user.service.RegistrationService;
import wiki.chiu.micro.user.service.UserAssetService;
import wiki.chiu.micro.user.service.UserExportService;
import wiki.chiu.micro.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  @Mock private RegistrationService registrationService;

  @Mock private UserAssetService assetService;

  @Mock private UserExportService exportService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    ValidatedRequest validation = new ValidatedRequest();
    UserHttpHandler handler =
        new UserHttpHandler(
            userService, registrationService, assetService, exportService, validation);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                UserRoutes.routes(
                    handler,
                    org.mockito.Mockito.mock(RoleHttpHandler.class),
                    org.mockito.Mockito.mock(MenuHttpHandler.class),
                    org.mockito.Mockito.mock(AuthorityHttpHandler.class),
                    org.mockito.Mockito.mock(UserInternalHttpHandler.class),
                    org.mockito.Mockito.mock(MenuInternalHttpHandler.class),
                    org.mockito.Mockito.mock(AuthorityInternalHttpHandler.class)))
            .build();
  }

  @Test
  void getRegisterPageReturnsToken() throws Exception {
    when(registrationService.issuePage("alice")).thenReturn("token-x");

    mockMvc
        .perform(get("/sys/user/auth/register/page").param("username", "alice"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("token-x"));
  }

  @Test
  void getRegisterPageMissingParamReturns400() throws Exception {
    mockMvc.perform(get("/sys/user/auth/register/page")).andExpect(status().isBadRequest());
  }

  @Test
  void checkRegisterPageReturnsBoolean() throws Exception {
    when(registrationService.isPageValid("tk")).thenReturn(true);

    mockMvc
        .perform(get("/sys/user/register/check").param("token", "tk"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  void imageUploadReturnsUrl() throws Exception {
    when(assetService.upload(anyString(), any())).thenReturn("https://oss/x.png");
    MockMultipartFile file = new MockMultipartFile("image", "x.png", "image/png", new byte[] {1});

    mockMvc
        .perform(multipart("/sys/user/register/image/upload").file(file).param("token", "tk"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value("https://oss/x.png"));
  }

  @Test
  void imageUploadRejectsEmptyFile() throws Exception {
    MockMultipartFile file = new MockMultipartFile("image", "x.png", "image/png", new byte[0]);

    mockMvc
        .perform(multipart("/sys/user/register/image/upload").file(file).param("token", "tk"))
        .andExpect(status().isBadRequest());

    verify(assetService, never()).upload(anyString(), any());
  }

  @Test
  void imageDeleteReturnsSuccess() throws Exception {
    doNothing().when(assetService).delete("tk", "https://oss/x.png");

    mockMvc
        .perform(
            delete("/sys/user/register/image/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://oss/x.png\",\"token\":\"tk\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void deleteReturnsSuccess() throws Exception {
    doNothing().when(userService).deleteUsers(any());

    mockMvc
        .perform(post("/sys/user/delete").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
        .andExpect(status().isOk());
  }

  @Test
  void invalidUserBodyIsRejectedBeforeHandler() throws Exception {
    mockMvc
        .perform(post("/sys/user/save").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("username must not be blank"));

    verify(userService, never()).saveOrUpdate(any());
  }

  @Test
  void registrationWithDifferentPasswordsIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"username\":\"alice\",\"nickname\":\"Alice\",\"password\":\"one\","
            + "\"confirmPassword\":\"two\",\"email\":\"alice@example.com\",\"token\":\"tk\"}";

    mockMvc
        .perform(
            post("/sys/user/register/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("passwords do not match"));

    verify(registrationService, never()).register(any());
  }

  @Test
  void registrationWithBlankPasswordsIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"username\":\"alice\",\"nickname\":\"Alice\",\"password\":\"   \","
            + "\"confirmPassword\":\"   \",\"email\":\"alice@example.com\",\"token\":\"tk\"}";

    mockMvc
        .perform(
            post("/sys/user/register/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("password must not be blank"));

    verify(registrationService, never()).register(any());
  }

  @Test
  void newUserWithoutPasswordIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"id\":null,\"username\":\"alice\",\"nickname\":\"Alice\","
            + "\"avatar\":\"https://example.com/avatar.png\",\"email\":\"alice@example.com\","
            + "\"phone\":\"13800000000\",\"status\":1,\"roles\":[\"ROLE_USER\"]}";

    mockMvc
        .perform(post("/sys/user/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("password required when creating user"));

    verify(userService, never()).saveOrUpdate(any());
  }

  @Test
  void newUserWithBlankPasswordIsRejectedBeforeHandler() throws Exception {
    String body =
        "{\"id\":null,\"username\":\"alice\",\"nickname\":\"Alice\","
            + "\"avatar\":\"https://example.com/avatar.png\",\"password\":\"   \","
            + "\"email\":\"alice@example.com\",\"phone\":\"13800000000\","
            + "\"status\":1,\"roles\":[\"ROLE_USER\"]}";

    mockMvc
        .perform(post("/sys/user/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("password required when creating user"));

    verify(userService, never()).saveOrUpdate(any());
  }

  @Test
  void deleteRejectsNullAndNonPositiveIds() throws Exception {
    mockMvc
        .perform(
            post("/sys/user/delete").contentType(MediaType.APPLICATION_JSON).content("[null,0]"))
        .andExpect(status().isBadRequest());

    verify(userService, never()).deleteUsers(any());
  }

  @Test
  void pageRejectsNonPositivePage() throws Exception {
    mockMvc.perform(get("/sys/user/page/0").param("size", "5")).andExpect(status().isBadRequest());

    verify(userService, never()).listPage(anyInt(), anyInt());
  }

  @Test
  void pageReturnsPage() throws Exception {
    when(userService.listPage(anyInt(), anyInt())).thenReturn(PageAdapter.emptyPage());

    mockMvc
        .perform(get("/sys/user/page/1").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.empty").value(true));
  }

  @Test
  void infoNotFoundReturns404() throws Exception {
    when(userService.findInfo(anyLong())).thenThrow(new MissException("not found"));

    mockMvc
        .perform(get("/sys/user/info/9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.msg").value("not found"));
  }

  @Test
  void unknownPathReturns404() throws Exception {
    mockMvc.perform(get("/sys/user/unknown")).andExpect(status().isNotFound());
  }
}
