package wiki.chiu.micro.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.adapter.out.token.JwtTokenService;
import wiki.chiu.micro.common.exception.MissException;

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
                config.websocketJwtDecoder(secretKey, properties),
                properties.issuer(),
                properties.audience(),
                new JwtTokenService.TokenLifetimes(
                    properties.accessTokenExpire(),
                    properties.refreshTokenExpire(),
                    properties.websocketTokenExpire()));
    }

    @Test
    void accessAndRefreshTokensAreNotInterchangeable() {
        String access = "Bearer " + tokens.accessToken(42L);
        String refresh = "Bearer " + tokens.refreshToken(42L);

        assertEquals(42L, tokens.resolveUserId("/api/private", access));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/api/private", refresh));
    }

    @Test
    void websocketTokenIsBoundToItsRoomClaim() {
        String ticket = "Bearer " + tokens.webSocketTicket(42L, "123");
        String access = "Bearer " + tokens.accessToken(42L);

        assertEquals(42L, tokens.resolveUserId("/rooms/123", ticket));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/rooms/456", ticket));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/rooms/123", access));
    }

    @Test
    void malformedAndMissingTokensAreRejected() {
        assertNull(tokens.resolveUserId("/api/private", null));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/api/private", "Bearer"));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/api/private", "Bearer x"));
    }
}
