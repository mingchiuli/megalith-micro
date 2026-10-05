package wiki.chiu.micro.auth.adapter.in.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.adapter.in.http.SessionCookies;
import wiki.chiu.micro.auth.application.model.SessionTokens;
import wiki.chiu.micro.auth.application.port.in.LoginSession;

@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock
    private LoginSession loginSession;

    @Mock
    private Authentication authentication;

    @Mock
    private FilterChain filterChain;

    private LoginSuccessHandler handler() {
        return new LoginSuccessHandler(
            JsonMapper.builder().build(),
            loginSession,
            new SessionCookies("/", true, "Strict", 900, 604800));
    }

    @Test
    void doesNotContinueFilterChainAfterWritingResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(
            new MockHttpServletRequest("POST", "/login"), response, filterChain, authentication);

        assertTrue(response.getContentAsString().contains("用户不存在"));

        verifyNoInteractions(filterChain);
    }

    @Test
    void storesTokensOnlyInHttpOnlyCookies() throws Exception {
        LoginUser user =
            new LoginUser(
                "tom",
                "password",
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                42L);
        when(authentication.getName()).thenReturn("tom");
        when(authentication.getPrincipal()).thenReturn(user);
        when(loginSession.start(42L, "tom")).thenReturn(new SessionTokens("access-jwt", "refresh-jwt"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler()
            .onAuthenticationSuccess(
                new MockHttpServletRequest("POST", "/login"), response, authentication);

        List<String> setCookies = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(
            setCookies.stream().anyMatch(value -> value.contains("megalith_access_token=access-jwt")));
        assertTrue(
            setCookies.stream()
                .anyMatch(value -> value.contains("megalith_refresh_token=refresh-jwt")));
        assertTrue(setCookies.stream().allMatch(value -> value.contains("HttpOnly")));
        assertTrue(setCookies.stream().allMatch(value -> value.contains("Secure")));
        assertTrue(setCookies.stream().allMatch(value -> value.contains("SameSite=Strict")));
        String body = response.getContentAsString();
        assertTrue(body.contains("\"code\":200"));
        assertTrue(body.contains("\"data\":null"));
        assertFalse(body.contains("access-jwt"));
        assertFalse(body.contains("refresh-jwt"));
        assertFalse(body.contains("accessToken"));
        assertFalse(body.contains("refreshToken"));
    }
}
