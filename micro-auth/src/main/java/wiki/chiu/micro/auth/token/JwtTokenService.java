package wiki.chiu.micro.auth.token;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder accessJwtDecoder;
    private final JwtDecoder refreshJwtDecoder;
    private final JwtDecoder websocketJwtDecoder;
    private final JwtProperties properties;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder,
                           @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder,
                           @Qualifier("websocketJwtDecoder") JwtDecoder websocketJwtDecoder,
                           JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.accessJwtDecoder = accessJwtDecoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
        this.websocketJwtDecoder = websocketJwtDecoder;
        this.properties = properties;
    }

    public String issueAccessToken(Long userId) {
        return issue(userId, TokenType.ACCESS, properties.accessTokenExpire(), Map.of());
    }

    public String issueRefreshToken(Long userId) {
        return issue(userId, TokenType.REFRESH, properties.refreshTokenExpire(), Map.of());
    }

    public String issueWebSocketToken(Long userId, String roomId) {
        return issue(userId, TokenType.WEBSOCKET, properties.websocketTokenExpire(),
                Map.of("room_id", roomId));
    }

    public Jwt decodeAccessToken(String token) {
        return accessJwtDecoder.decode(stripBearerPrefix(token));
    }

    public Jwt decodeRefreshToken(String token) {
        return refreshJwtDecoder.decode(stripBearerPrefix(token));
    }

    public Jwt decodeWebSocketToken(String token) {
        return websocketJwtDecoder.decode(stripBearerPrefix(token));
    }

    private String issue(Long userId, TokenType type, long expiresInSeconds, Map<String, Object> claims) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .audience(List.of(properties.audience()))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(issuedAt.plusSeconds(expiresInSeconds))
                .id(UUID.randomUUID().toString())
                .claim("token_use", type.value());
        claims.forEach(claimsBuilder::claim);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512)
                .type("JWT")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claimsBuilder.build())).getTokenValue();
    }

    private String stripBearerPrefix(String token) {
        if (token == null || !token.startsWith("Bearer ") || token.length() == "Bearer ".length()) {
            throw new IllegalArgumentException("Invalid bearer token");
        }
        return token.substring("Bearer ".length());
    }
}
