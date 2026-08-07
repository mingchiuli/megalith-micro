package wiki.chiu.micro.auth.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "megalith.auth.cookie")
public record TokenCookieProperties(
        @NotBlank @Pattern(regexp = "/.*") String path,
        boolean secure,
        @NotBlank String sameSite) {
}
