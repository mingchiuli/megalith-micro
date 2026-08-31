package wiki.chiu.micro.auth.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RefreshTokenCookieManagerTest {

    private RefreshTokenCookieManager cookieManager;

    @BeforeEach
    void setUp() {
        cookieManager =
            new RefreshTokenCookieManager(
                new TokenCookieProperties("/", true, "Strict"),
                new JwtProperties(
                    "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                    900,
                    604800,
                    300,
                    "micro-auth",
                    "megalith-api"));
    }

    @Test
    void createsHostOnlyHttpOnlyRefreshCookie() {
        String cookie = cookieManager.create("refresh-jwt").toString();

        assertTrue(cookie.contains("megalith_refresh_token=refresh-jwt"));
        assertTrue(cookie.contains("Max-Age=604800"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void resolvesOnlyConfiguredCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "ignored"), new Cookie("megalith_refresh_token", "jwt"));

        assertEquals("jwt", cookieManager.resolve(request));
        assertNull(cookieManager.resolve(new MockHttpServletRequest()));
    }
}
