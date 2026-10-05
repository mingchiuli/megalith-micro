package wiki.chiu.micro.auth.adapter.in.http;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

/**
 * Shapes the token cookies the browser stores. The policy comes from
 * {@code megalith.auth.cookie} and the JWT lifetimes, both injected by the composition root.
 */
public final class SessionCookies {

    public static final String ACCESS_COOKIE_NAME = "megalith_access_token";

    public static final String REFRESH_COOKIE_NAME = "megalith_refresh_token";

    private final String path;

    private final boolean secure;

    private final String sameSite;

    private final Duration accessMaxAge;

    private final Duration refreshMaxAge;

    public SessionCookies(
        String path, boolean secure, String sameSite, long accessMaxAgeSeconds, long refreshMaxAgeSeconds) {
        this.path = path;
        this.secure = secure;
        this.sameSite = sameSite;
        this.accessMaxAge = Duration.ofSeconds(accessMaxAgeSeconds);
        this.refreshMaxAge = Duration.ofSeconds(refreshMaxAgeSeconds);
    }

    public ResponseCookie access(String accessToken) {
        return cookie(ACCESS_COOKIE_NAME, accessToken, accessMaxAge);
    }

    public ResponseCookie expiredAccess() {
        return cookie(ACCESS_COOKIE_NAME, "", Duration.ZERO);
    }

    public ResponseCookie refresh(String refreshToken) {
        return cookie(REFRESH_COOKIE_NAME, refreshToken, refreshMaxAge);
    }

    public ResponseCookie expiredRefresh() {
        return cookie(REFRESH_COOKIE_NAME, "", Duration.ZERO);
    }

    private ResponseCookie cookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path(path)
            .maxAge(maxAge)
            .build();
    }
}
