package wiki.chiu.micro.auth.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.auth.handler.AuthHttpHandler;
import wiki.chiu.micro.auth.handler.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.handler.CodeHttpHandler;
import wiki.chiu.micro.auth.handler.TokenHttpHandler;
import wiki.chiu.micro.auth.route.AuthRoutes;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.exception.CodeException;

@ExtendWith(MockitoExtension.class)
class CodeControllerTest {

  @Mock private CodeService codeService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    CodeHttpHandler handler = new CodeHttpHandler(codeService);
    mockMvc =
        MockMvcBuilders.routerFunctions(
                AuthRoutes.routes(
                    org.mockito.Mockito.mock(AuthHttpHandler.class),
                    org.mockito.Mockito.mock(TokenHttpHandler.class),
                    handler,
                    org.mockito.Mockito.mock(AuthInternalHttpHandler.class)))
            .build();
  }

  @Test
  void createEmailCodeReturnsSuccess() throws Exception {
    doNothing().when(codeService).createEmailCode("a@b.com");

    mockMvc
        .perform(
            post("/code/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"a@b.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    verify(codeService).createEmailCode("a@b.com");
  }

  @Test
  void createEmailCodeMissingParamReturns400() throws Exception {
    mockMvc
        .perform(post("/code/email").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createEmailCodeBlankParamReturns400() throws Exception {
    mockMvc
        .perform(
            post("/code/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"   \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createEmailCodeWhenServiceThrowsReturns400() throws Exception {
    doThrow(new CodeException("send failed")).when(codeService).createEmailCode("x@y.com");

    mockMvc
        .perform(
            post("/code/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"x@y.com\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.msg").value("send failed"));
  }

  @Test
  void createSmsCodeReturnsSuccess() throws Exception {
    doNothing().when(codeService).createSMSCode("13800000000");

    mockMvc
        .perform(
            post("/code/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"13800000000\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    verify(codeService).createSMSCode("13800000000");
  }

  @Test
  void unknownCodePathReturns404() throws Exception {
    mockMvc.perform(get("/code/unknown")).andExpect(status().isNotFound());
  }
}
