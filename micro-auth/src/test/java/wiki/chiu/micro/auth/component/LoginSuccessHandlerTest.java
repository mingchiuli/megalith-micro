package wiki.chiu.micro.auth.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.AccessTokenCookieManager;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.auth.token.TokenCookieProperties;
import wiki.chiu.micro.auth.user.LoginUser;

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
            cookieProperties(),
            new JwtProperties(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                900,
                604800,
                300,
                "micro-auth",
                "megalith-api"));
    }

    private AccessTokenCookieManager accessCookieManager() {
        return new AccessTokenCookieManager(
            cookieProperties(),
            new JwtProperties(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                900,
                604800,
                300,
                "micro-auth",
                "megalith-api"));
    }

    private TokenCookieProperties cookieProperties() {
        return new TokenCookieProperties("/", true, "Strict");
    }

    @Test
    void doesNotContinueFilterChainAfterWritingResponse() throws Exception {
        LoginSuccessHandler handler =
            new LoginSuccessHandler(
                JsonMapper.builder().build(),
                jwtTokenService,
                userHttpServiceWrapper,
                redissonClient,
                cookieManager(),
                accessCookieManager());
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
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
        RKeys keys = mock(RKeys.class);
        when(authentication.getName()).thenReturn("tom");
        when(authentication.getPrincipal()).thenReturn(user);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(jwtTokenService.issueAccessToken(42L)).thenReturn("access-jwt");
        when(jwtTokenService.issueRefreshToken(42L)).thenReturn("refresh-jwt");

        LoginSuccessHandler handler =
            new LoginSuccessHandler(
                JsonMapper.builder().build(),
                jwtTokenService,
                userHttpServiceWrapper,
                redissonClient,
                cookieManager(),
                accessCookieManager());
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
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
