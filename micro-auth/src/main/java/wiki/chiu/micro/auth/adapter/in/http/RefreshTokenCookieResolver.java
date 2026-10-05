package wiki.chiu.micro.auth.adapter.in.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * Reads the refresh token from its HttpOnly cookie so the refresh chain can verify it without a
 * request body.
 */
@Component
public class RefreshTokenCookieResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, SessionCookies.REFRESH_COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            return null;
        }
        return cookie.getValue();
    }
}
