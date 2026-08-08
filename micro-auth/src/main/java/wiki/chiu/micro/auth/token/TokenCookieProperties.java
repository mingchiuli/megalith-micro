package wiki.chiu.micro.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "megalith.auth.cookie")
public record TokenCookieProperties(
        String path,
        boolean secure,
        String sameSite) {

    public TokenCookieProperties {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("megalith.auth.cookie.path must not be blank");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("megalith.auth.cookie.path must start with /");
        }
        if (sameSite == null || sameSite.isBlank()) {
            throw new IllegalArgumentException("megalith.auth.cookie.same-site must not be blank");
        }
    }
}
