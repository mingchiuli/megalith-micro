package wiki.chiu.micro.auth.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
public class RefreshTokenCookieManager implements BearerTokenResolver {

    private static final String COOKIE_NAME = "__Secure-refresh_token";
    private static final String SAME_SITE = "Strict";

    private final RefreshTokenCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public RefreshTokenCookieManager(RefreshTokenCookieProperties cookieProperties,
                                     JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie create(String refreshToken) {
        return cookie(refreshToken, Duration.ofSeconds(jwtProperties.refreshTokenExpire()));
    }

    public ResponseCookie expire() {
        return cookie("", Duration.ZERO);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            return null;
        }
        return cookie.getValue();
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE)
                .path(cookieProperties.path())
                .maxAge(maxAge)
                .build();
    }
}
