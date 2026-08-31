package wiki.chiu.micro.auth.token;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenCookieManager {

    public static final String COOKIE_NAME = "megalith_access_token";

    private final TokenCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public AccessTokenCookieManager(
        TokenCookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie create(String accessToken) {
        return cookie(accessToken, Duration.ofSeconds(jwtProperties.accessTokenExpire()));
    }

    public ResponseCookie expire() {
        return cookie("", Duration.ZERO);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
            .httpOnly(true)
            .secure(cookieProperties.secure())
            .sameSite(cookieProperties.sameSite())
            .path(cookieProperties.path())
            .maxAge(maxAge)
            .build();
    }
}
