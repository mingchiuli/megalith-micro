package wiki.chiu.micro.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "megalith.auth.jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpire,
    long refreshTokenExpire,
    long websocketTokenExpire,
    String issuer,
    String audience) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("megalith.auth.jwt.secret must not be blank");
        }
        if (secret.length() < 64) {
            throw new IllegalArgumentException("megalith.auth.jwt.secret size must be at least 64");
        }
        if (accessTokenExpire <= 0) {
            throw new IllegalArgumentException("megalith.auth.jwt.access-token-expire must be positive");
        }
        if (refreshTokenExpire <= 0) {
            throw new IllegalArgumentException("megalith.auth.jwt.refresh-token-expire must be positive");
        }
        if (websocketTokenExpire <= 0) {
            throw new IllegalArgumentException(
                "megalith.auth.jwt.websocket-token-expire must be positive");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("megalith.auth.jwt.issuer must not be blank");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("megalith.auth.jwt.audience must not be blank");
        }
    }
}
