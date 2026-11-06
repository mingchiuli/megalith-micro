package wiki.chiu.micro.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.adapter.out.token.JwtTokenService;
import wiki.chiu.micro.common.exception.MissException;

class JwtConfigTest {

    private JwtTokenService tokens;
    private org.springframework.security.oauth2.jwt.JwtEncoder encoder;

    @BeforeEach
    void setUp() {
        JwtProperties properties =
            new JwtProperties(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                900,
                604800,
                60,
                "micro-auth",
                "megalith-api");
        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        encoder = config.jwtEncoder(secretKey);
        tokens =
            new JwtTokenService(
                config.jwtEncoder(secretKey),
                config.accessJwtDecoder(secretKey, properties),
                config.websocketJwtDecoder(secretKey, properties),
                properties.issuer(),
                properties.audience(),
                new JwtTokenService.TokenLifetimes(
                    properties.accessTokenExpire(),
                    properties.refreshTokenExpire()));
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
        String ticket = "Bearer " + tokens.webSocketTicket(42L, "123", System.currentTimeMillis() + 60_000);
        String access = "Bearer " + tokens.accessToken(42L);

        assertEquals(42L, tokens.resolveUserId("/rooms/123", ticket));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/rooms/456", ticket));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/rooms/123", access));
    }

    @Test
    void collaborationDeadlineCannotBeExtendedOrReplayedAfterExpiry() {
        long deadline = (System.currentTimeMillis() + 20_000) / 1000 * 1000;
        String ticket = "Bearer " + tokens.webSocketTicket(42L, "room", deadline);
        assertEquals(deadline, tokens.authorizationExpiresAt("/rooms/room", ticket));
        assertThrows(IllegalArgumentException.class, () -> tokens.webSocketTicket(42L, "room", System.currentTimeMillis() + 120_000));
        assertThrows(IllegalArgumentException.class, () -> tokens.webSocketTicket(42L, "room", System.currentTimeMillis() - 1));
    }

    @Test
    void expiredTicketsAreRejectedEvenInsideTheJwtDecodersClockSkew() {
        var now = Instant.now();
        for (var expiry : List.of(now.minusSeconds(1), now.plusSeconds(120))) {
            var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("micro-auth").audience(List.of("megalith-api")).subject("42")
                .issuedAt(now.minusSeconds(10)).notBefore(now.minusSeconds(10)).expiresAt(expiry)
                .claim("token_use", "websocket").claim("room_id", "room").build();
            var header = org.springframework.security.oauth2.jwt.JwsHeader.with(
                org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512).type("JWT").build();
            String ticket = "Bearer " + encoder.encode(
                org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(header, claims)).getTokenValue();
            assertThrows(MissException.class, () -> tokens.resolveUserId("/rooms/room", ticket));
        }
    }

    @Test
    void malformedAndMissingTokensAreRejected() {
        assertNull(tokens.resolveUserId("/api/private", null));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/api/private", "Bearer"));
        assertThrows(MissException.class, () -> tokens.resolveUserId("/api/private", "Bearer x"));
    }
}
