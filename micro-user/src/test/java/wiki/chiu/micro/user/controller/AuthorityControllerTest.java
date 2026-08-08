package wiki.chiu.micro.user.controller;

import static org.mockito.ArgumentMatchers.any;
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
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.handler.AuthorityHttpHandler;
import wiki.chiu.micro.user.handler.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.handler.MenuHttpHandler;
import wiki.chiu.micro.user.handler.MenuInternalHttpHandler;
import wiki.chiu.micro.user.handler.RoleHttpHandler;
import wiki.chiu.micro.user.handler.UserHttpHandler;
import wiki.chiu.micro.user.handler.UserInternalHttpHandler;
import wiki.chiu.micro.user.route.UserRoutes;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.vo.AuthorityVo;

@ExtendWith(MockitoExtension.class)
class AuthorityControllerTest {

  @Mock private AuthorityService authorityService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    ValidatedRequest validation = new ValidatedRequest();
    AuthorityHttpHandler handler = new AuthorityHttpHandler(authorityService, validation);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                UserRoutes.routes(
                    org.mockito.Mockito.mock(UserHttpHandler.class),
                    org.mockito.Mockito.mock(RoleHttpHandler.class),
                    org.mockito.Mockito.mock(MenuHttpHandler.class),
                    handler,
                    org.mockito.Mockito.mock(UserInternalHttpHandler.class),
                    org.mockito.Mockito.mock(MenuInternalHttpHandler.class),
                    org.mockito.Mockito.mock(AuthorityInternalHttpHandler.class)))
            .build();
  }

  @Test
  void listReturnsList() throws Exception {
    when(authorityService.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/sys/authority/list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void infoReturnsAuthority() throws Exception {
    AuthorityVo vo = AuthorityVo.builder().id(1L).code("READ").build();
    when(authorityService.findById(1L)).thenReturn(vo);

    mockMvc
        .perform(get("/sys/authority/info/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.code").value("READ"));
  }

  @Test
  void infoNotFoundReturns404() throws Exception {
    when(authorityService.findById(anyLong())).thenThrow(new MissException("not found"));

    mockMvc
        .perform(get("/sys/authority/info/9"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(1))
        .andExpect(jsonPath("$.msg").value("not found"));
  }

  @Test
  void deleteReturnsSuccess() throws Exception {
    doNothing().when(authorityService).deleteAuthorities(any());

    mockMvc
        .perform(
            post("/sys/authority/delete").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void saveRejectsInvalidServicePort() throws Exception {
    String body =
        "{\"id\":null,\"code\":\"READ\",\"remark\":\"read\","
            + "\"prototype\":\"HTTP\",\"methodType\":\"GET\","
            + "\"routePattern\":\"/read\",\"serviceHost\":\"localhost\","
            + "\"servicePort\":70000,\"type\":0,\"status\":1}";

    mockMvc
        .perform(post("/sys/authority/save").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());

    verify(authorityService, never()).saveOrUpdate(any());
  }

  @Test
  void downloadReturnsBytes() throws Exception {
    when(authorityService.download()).thenReturn(new byte[] {9});

    mockMvc
        .perform(get("/sys/authority/download"))
        .andExpect(status().isOk())
        .andExpect(content().bytes(new byte[] {9}));
  }

  @Test
  void unknownPathReturns404() throws Exception {
    mockMvc.perform(get("/sys/authority/unknown")).andExpect(status().isNotFound());
  }
}
