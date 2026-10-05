package wiki.chiu.micro.auth.adapter.in.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RefreshTokenCookieResolverTest {

    private final RefreshTokenCookieResolver resolver = new RefreshTokenCookieResolver();

    @Test
    void resolvesOnlyConfiguredCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "ignored"), new Cookie("megalith_refresh_token", "jwt"));

        assertEquals("jwt", resolver.resolve(request));
        assertNull(resolver.resolve(new MockHttpServletRequest()));
    }
}
