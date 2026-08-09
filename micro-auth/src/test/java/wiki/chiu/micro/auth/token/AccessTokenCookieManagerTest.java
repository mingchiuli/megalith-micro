package wiki.chiu.micro.auth.token;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessTokenCookieManagerTest {

  private AccessTokenCookieManager cookieManager;

  @BeforeEach
  void setUp() {
    cookieManager =
        new AccessTokenCookieManager(
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
  void storesRawTokenInHttpOnlyCookie() {
    String cookie = cookieManager.create("access-jwt").toString();

    assertTrue(cookie.contains("megalith_access_token=access-jwt"));
    assertTrue(cookie.contains("Max-Age=900"));
    assertTrue(cookie.contains("Path=/"));
    assertTrue(cookie.contains("HttpOnly"));
    assertTrue(cookie.contains("Secure"));
    assertTrue(cookie.contains("SameSite=Strict"));
  }
}
