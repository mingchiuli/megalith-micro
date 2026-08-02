package wiki.chiu.micro.auth.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "megalith.auth.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 64) String secret,
        @Positive long accessTokenExpire,
        @Positive long refreshTokenExpire,
        @Positive long websocketTokenExpire,
        @NotBlank String issuer,
        @NotBlank String audience) {
}
