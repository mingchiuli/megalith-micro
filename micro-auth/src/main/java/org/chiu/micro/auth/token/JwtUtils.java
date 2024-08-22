package org.chiu.micro.auth.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.SneakyThrows;

import org.chiu.micro.auth.exception.AuthException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static org.chiu.micro.auth.lang.Const.*;
import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * jwt工具类
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.jwt")
public class JwtUtils implements TokenUtils<Claims> {

    private String secret;

    private Algorithm algorithm;

    private JWSVerifier verifier;

    private JWSSigner signer;

    private final ObjectMapper objectMapper;

    @PostConstruct
    @SneakyThrows
    public void init() {
        verifier = new MACVerifier(secret);
        signer = new MACSigner(secret);
    }

    @SneakyThrows
    public String generateToken(String userId, List<String> roles, long expire) {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS512)
                .type(JOSEObjectType.JWT)
                .build();

        var nowDate = new Date();
        // 过期时间
        roles = roles.stream()
                .map(role -> ROLE_PREFIX.getInfo() + role)
                .toList();
        var expireDate = new Date(nowDate.getTime() + expire * 1000);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer("micro-auth")
                .subject(userId)
                .claim("roles", roles)
                .issueTime(nowDate)
                .expirationTime(expireDate)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }

    @SuppressWarnings("unchecked")
    public Claims getVerifierByToken(String token) throws AuthException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                throw new AuthException(AUTH_EXCEPTION.getMsg());
            }
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = jwtClaimsSet.getExpirationTime();
            if (new Date().after(expirationTime)) {
                throw new AuthException(TOKEN_INVALID.getMsg());
            }
            var claim = new Claims();

            var roles = jwtClaimsSet.getClaim("roles");
            claim.setUserId(jwtClaimsSet.getSubject());
            claim.setRoles((List<String>) roles);
            return claim;
        } catch (JOSEException | ParseException e) {
            throw new AuthException(TOKEN_INVALID.getMsg());
        }
    }
}
