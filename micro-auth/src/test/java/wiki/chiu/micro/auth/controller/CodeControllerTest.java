package wiki.chiu.micro.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.auth.exception.GlobalExceptionHandler;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.exception.CodeException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CodeControllerTest {

    @Mock
    private CodeService codeService;

    @InjectMocks
    private CodeController codeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(codeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createEmailCodeReturnsSuccess() throws Exception {
        doNothing().when(codeService).createEmailCode("a@b.com");

        mockMvc.perform(get("/code/email").param("loginName", "a@b.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(codeService).createEmailCode("a@b.com");
    }

    @Test
    void createEmailCodeMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/code/email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmailCodeWhenServiceThrowsReturns400() throws Exception {
        doThrow(new CodeException("send failed")).when(codeService).createEmailCode("x@y.com");

        mockMvc.perform(get("/code/email").param("loginName", "x@y.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("send failed"));
    }

    @Test
    void createSmsCodeReturnsSuccess() throws Exception {
        doNothing().when(codeService).createSMSCode("13800000000");

        mockMvc.perform(get("/code/sms").param("loginName", "13800000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(codeService).createSMSCode("13800000000");
    }

    @Test
    void unknownCodePathReturns404() throws Exception {
        mockMvc.perform(get("/code/unknown"))
                .andExpect(status().isNotFound());
    }
}
