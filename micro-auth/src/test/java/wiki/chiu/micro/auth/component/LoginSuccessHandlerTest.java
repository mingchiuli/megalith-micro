package wiki.chiu.micro.auth.component;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.redisson.api.RKeys;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.auth.token.RefreshTokenCookieProperties;
import wiki.chiu.micro.auth.user.LoginUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserHttpServiceWrapper userHttpServiceWrapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private Authentication authentication;

    @Mock
    private FilterChain filterChain;

    private RefreshTokenCookieManager cookieManager() {
        return new RefreshTokenCookieManager(
                new RefreshTokenCookieProperties("/api/token/refresh"),
                new JwtProperties(
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                        900, 604800, 300, "micro-auth", "megalith-api"));
    }

    @Test
    void doesNotContinueFilterChainAfterWritingResponse() throws Exception {
        LoginSuccessHandler handler = new LoginSuccessHandler(
                JsonMapper.builder().build(), jwtTokenService, userHttpServiceWrapper,
                redissonClient, cookieManager());
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest("POST", "/login"),
                response,
                filterChain,
                authentication);

        assertTrue(response.getContentAsString().contains("用户不存在"));
        verifyNoInteractions(filterChain);
    }

    @Test
    void storesRefreshTokenOnlyInHttpOnlyCookie() throws Exception {
        LoginUser user = new LoginUser(
                "tom", "password", true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")), 42L);
        RKeys keys = mock(RKeys.class);
        when(authentication.getName()).thenReturn("tom");
        when(authentication.getPrincipal()).thenReturn(user);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(jwtTokenService.issueAccessToken(42L)).thenReturn("access-jwt");
        when(jwtTokenService.issueRefreshToken(42L)).thenReturn("refresh-jwt");

        LoginSuccessHandler handler = new LoginSuccessHandler(
                JsonMapper.builder().build(), jwtTokenService, userHttpServiceWrapper,
                redissonClient, cookieManager());
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest("POST", "/login"), response, authentication);

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertTrue(setCookie.contains("__Secure-refresh_token=refresh-jwt"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=Strict"));
        assertTrue(response.getContentAsString().contains("Bearer access-jwt"));
        assertFalse(response.getContentAsString().contains("refresh-jwt"));
        assertFalse(response.getContentAsString().contains("refreshToken"));
    }
}
