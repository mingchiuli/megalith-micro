package wiki.chiu.micro.auth.adapter.out.token;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import wiki.chiu.micro.auth.application.port.out.RouteTokenReader;
import wiki.chiu.micro.auth.application.port.out.TokenEncoder;
import wiki.chiu.micro.auth.domain.TokenType;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;

/**
 * Mints and reads the HS512 tokens this service issues. Wired by
 * {@code config/JwtConfig} so the JWT settings stay in the composition root.
 */
public final class JwtTokenService implements TokenEncoder, RouteTokenReader {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String ROOM_CLAIM = "room_id";

    private static final String WEBSOCKET_ROUTE_PREFIX = "/rooms/";

    private static final String TOKEN_USE_CLAIM = "token_use";

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder accessJwtDecoder;

    private final JwtDecoder refreshJwtDecoder;

    private final JwtDecoder websocketJwtDecoder;

    private final String issuer;

    private final String audience;

    private final TokenLifetimes lifetimes;

    public JwtTokenService(
        JwtEncoder jwtEncoder,
        JwtDecoder accessJwtDecoder,
        JwtDecoder refreshJwtDecoder,
        JwtDecoder websocketJwtDecoder,
        String issuer,
        String audience,
        TokenLifetimes lifetimes) {
        this.jwtEncoder = jwtEncoder;
        this.accessJwtDecoder = accessJwtDecoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
        this.websocketJwtDecoder = websocketJwtDecoder;
        this.issuer = issuer;
        this.audience = audience;
        this.lifetimes = lifetimes;
    }

    /** How long each token flavour stays valid. */
    public record TokenLifetimes(
        long accessSeconds, long refreshSeconds, long webSocketSeconds) {
    }

    @Override
    public String accessToken(Long userId) {
        return issue(userId, TokenType.ACCESS, lifetimes.accessSeconds(), Map.of());
    }

    @Override
    public String refreshToken(Long userId) {
        return issue(userId, TokenType.REFRESH, lifetimes.refreshSeconds(), Map.of());
    }

    @Override
    public String webSocketTicket(Long userId, String roomId) {
        return issue(
            userId, TokenType.WEBSOCKET, lifetimes.webSocketSeconds(), Map.of(ROOM_CLAIM, roomId));
    }

    @Override
    public Long resolveUserId(String routeMapping, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return subject(decode(routeMapping, token));
        } catch (JwtException | IllegalArgumentException e) {
            throw new MissException(ExceptionMessage.TOKEN_INVALID);
        }
    }

    private Jwt decode(String routeMapping, String token) {
        if (!isWebSocketRoute(routeMapping)) {
            return accessJwtDecoder.decode(stripBearerPrefix(token));
        }
        Jwt jwt = websocketJwtDecoder.decode(stripBearerPrefix(token));
        String roomId = routeMapping.substring(WEBSOCKET_ROUTE_PREFIX.length());
        if (roomId.isEmpty() || !roomId.equals(jwt.getClaimAsString(ROOM_CLAIM))) {
            throw new IllegalArgumentException("WebSocket ticket does not match the room");
        }
        return jwt;
    }

    private boolean isWebSocketRoute(String routeMapping) {
        return routeMapping.startsWith(WEBSOCKET_ROUTE_PREFIX);
    }

    private Long subject(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid JWT subject", e);
        }
    }

    private String issue(
        Long userId, TokenType type, long expiresInSeconds, Map<String, Object> claims) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet.Builder claimsBuilder =
            JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(issuedAt.plusSeconds(expiresInSeconds))
                .id(UUID.randomUUID().toString())
                .claim(TOKEN_USE_CLAIM, type.value());
        claims.forEach(claimsBuilder::claim);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).type("JWT").build();
        return jwtEncoder
            .encode(JwtEncoderParameters.from(header, claimsBuilder.build()))
            .getTokenValue();
    }

    private String stripBearerPrefix(String token) {
        if (!token.startsWith(BEARER_PREFIX) || token.length() == BEARER_PREFIX.length()) {
            throw new IllegalArgumentException("Invalid bearer token");
        }
        return token.substring(BEARER_PREFIX.length());
    }
}
