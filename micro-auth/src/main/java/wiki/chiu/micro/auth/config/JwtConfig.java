package wiki.chiu.micro.auth.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.TokenCookieProperties;
import wiki.chiu.micro.auth.token.TokenType;

import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({JwtProperties.class, TokenCookieProperties.class})
public class JwtConfig {

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean("accessJwtDecoder")
    JwtDecoder accessJwtDecoder(SecretKey jwtSecretKey, JwtProperties properties) {
        return decoder(jwtSecretKey, properties, TokenType.ACCESS);
    }

    @Bean("refreshJwtDecoder")
    JwtDecoder refreshJwtDecoder(SecretKey jwtSecretKey, JwtProperties properties) {
        return decoder(jwtSecretKey, properties, TokenType.REFRESH);
    }

    @Bean("websocketJwtDecoder")
    JwtDecoder websocketJwtDecoder(SecretKey jwtSecretKey, JwtProperties properties) {
        return decoder(jwtSecretKey, properties, TokenType.WEBSOCKET);
    }

    private JwtDecoder decoder(SecretKey secretKey, JwtProperties properties, TokenType tokenType) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .validateType(true)
                .build();
        var validator = new DelegatingOAuth2TokenValidator<Jwt>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                new JwtAudienceValidator(properties.audience()),
                new JwtClaimValidator<>("token_use", tokenType.value()::equals));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
