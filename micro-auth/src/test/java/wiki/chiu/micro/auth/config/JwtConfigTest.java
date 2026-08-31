package wiki.chiu.micro.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.JwtTokenService;

class JwtConfigTest {

    private JwtTokenService tokens;

    @BeforeEach
    void setUp() {
        JwtProperties properties =
            new JwtProperties(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                900,
                604800,
                300,
                "micro-auth",
                "megalith-api");
        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        tokens =
            new JwtTokenService(
                config.jwtEncoder(secretKey),
                config.accessJwtDecoder(secretKey, properties),
                config.refreshJwtDecoder(secretKey, properties),
                config.websocketJwtDecoder(secretKey, properties),
                properties);
    }

    @Test
    void accessAndRefreshTokensAreNotInterchangeable() {
        String access = "Bearer " + tokens.issueAccessToken(42L);
        String refresh = "Bearer " + tokens.issueRefreshToken(42L);

        assertEquals("42", tokens.decodeAccessToken(access).getSubject());
        assertEquals("42", tokens.decodeRefreshToken(refresh).getSubject());
        assertThrows(JwtException.class, () -> tokens.decodeRefreshToken(access));
        assertThrows(JwtException.class, () -> tokens.decodeAccessToken(refresh));
    }

    @Test
    void websocketTokenIsBoundToItsRoomClaim() {
        String token = "Bearer " + tokens.issueWebSocketToken(42L, "123");

        assertEquals("123", tokens.decodeWebSocketToken(token).getClaimAsString("room_id"));
        assertThrows(JwtException.class, () -> tokens.decodeAccessToken(token));
    }
}
