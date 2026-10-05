package wiki.chiu.micro.auth.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionCookiesTest {

    private SessionCookies cookies;

    @BeforeEach
    void setUp() {
        cookies = new SessionCookies("/", true, "Strict", 900, 604800);
    }

    @Test
    void storesRawAccessTokenInHttpOnlyCookie() {
        String cookie = cookies.access("access-jwt").toString();

        assertTrue(cookie.contains("megalith_access_token=access-jwt"));
        assertTrue(cookie.contains("Max-Age=900"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void storesRawRefreshTokenInHttpOnlyCookie() {
        String cookie = cookies.refresh("refresh-jwt").toString();

        assertTrue(cookie.contains("megalith_refresh_token=refresh-jwt"));
        assertTrue(cookie.contains("Max-Age=604800"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
    }

    @Test
    void expiresBothCookiesOnLogout() {
        assertTrue(cookies.expiredAccess().toString().contains("megalith_access_token="));
        assertTrue(cookies.expiredAccess().toString().contains("Max-Age=0"));
        assertTrue(cookies.expiredRefresh().toString().contains("megalith_refresh_token="));
        assertTrue(cookies.expiredRefresh().toString().contains("Max-Age=0"));
    }
}
