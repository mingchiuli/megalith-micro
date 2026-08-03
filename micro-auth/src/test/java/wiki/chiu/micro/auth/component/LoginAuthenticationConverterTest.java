package wiki.chiu.micro.auth.component;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.component.token.EmailAuthenticationToken;
import wiki.chiu.micro.auth.component.token.SMSAuthenticationToken;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAuthenticationConverterTest {

    private final LoginAuthenticationConverter converter =
            new LoginAuthenticationConverter(JsonMapper.builder().build());

    @Test
    void isNotAResourceServerAuthenticationConverterCandidate() {
        assertFalse(AuthenticationConverter.class.isAssignableFrom(LoginAuthenticationConverter.class));
    }

    @Test
    void convertsPasswordLogin() {
        Authentication authentication = convert("""
                {
                  "loginType": "PASSWORD",
                  "principal": "tom",
                  "credential": "secret"
                }
                """);

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertEquals("tom", authentication.getPrincipal());
        assertEquals("secret", authentication.getCredentials());
    }

    @Test
    void convertsEmailLogin() {
        Authentication authentication = convert("""
                {
                  "loginType": "EMAIL",
                  "principal": "tom@example.com",
                  "credential": "123456"
                }
                """);

        assertInstanceOf(EmailAuthenticationToken.class, authentication);
        assertEquals("tom@example.com", authentication.getPrincipal());
        assertEquals("123456", authentication.getCredentials());
    }

    @Test
    void convertsSmsLogin() {
        Authentication authentication = convert("""
                {
                  "loginType": "SMS",
                  "principal": "13800000000",
                  "credential": "123456"
                }
                """);

        assertInstanceOf(SMSAuthenticationToken.class, authentication);
        assertEquals("13800000000", authentication.getPrincipal());
        assertEquals("123456", authentication.getCredentials());
    }

    @Test
    void rejectsMalformedLoginRequest() {
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> convert("{not-json}"));

        assertEquals("非法登录", exception.getMessage());
    }

    @Test
    void rejectsNullLoginRequest() {
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> convert("null"));

        assertEquals("非法登录", exception.getMessage());
    }

    @Test
    void rejectsIncompleteLoginRequest() {
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> convert("""
                        {
                          "loginType": "PASSWORD",
                          "principal": "tom"
                        }
                        """));

        assertEquals("非法登录", exception.getMessage());
    }

    private Authentication convert(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return converter.convert(request);
    }
}
