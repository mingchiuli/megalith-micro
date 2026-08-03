package wiki.chiu.micro.auth.token;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenCookieManagerTest {

    private RefreshTokenCookieManager cookieManager;

    @BeforeEach
    void setUp() {
        cookieManager = new RefreshTokenCookieManager(
                new RefreshTokenCookieProperties("/api/token/refresh"),
                new JwtProperties(
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                        900, 604800, 300, "micro-auth", "megalith-api"));
    }

    @Test
    void createsHostOnlyHttpOnlyRefreshCookie() {
        String cookie = cookieManager.create("refresh-jwt").toString();

        assertTrue(cookie.contains("__Secure-refresh_token=refresh-jwt"));
        assertTrue(cookie.contains("Max-Age=604800"));
        assertTrue(cookie.contains("Path=/api/token/refresh"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void resolvesOnlyConfiguredCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "ignored"), new Cookie("__Secure-refresh_token", "jwt"));

        assertEquals("jwt", cookieManager.resolve(request));
        assertNull(cookieManager.resolve(new MockHttpServletRequest()));
    }
}
