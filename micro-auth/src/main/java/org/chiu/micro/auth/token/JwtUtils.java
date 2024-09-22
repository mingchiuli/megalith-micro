package org.chiu.micro.auth.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.exception.MissException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.chiu.micro.auth.lang.Const.ROLE_PREFIX;
import static org.chiu.micro.auth.lang.ExceptionMessage.AUTH_EXCEPTION;
import static org.chiu.micro.auth.lang.ExceptionMessage.TOKEN_INVALID;

/**
 * jwt工具类
 */
@Component
@ConfigurationProperties(prefix = "blog.jwt")
public class JwtUtils implements TokenUtils<Claims> {

    private String secret;

    private Algorithm algorithm;

    private JWSVerifier verifier;

    private JWSSigner signer;

    private final ObjectMapper objectMapper;

    public JwtUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws JOSEException {
        verifier = new MACVerifier(secret);
        signer = new MACSigner(secret);
    }

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
        try {
            jwsObject.sign(signer);
        } catch (JOSEException e) {
            throw new MissException(e.getMessage());
        }
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

    public String getSecret() {
        return this.secret;
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    public JWSVerifier getVerifier() {
        return this.verifier;
    }

    public JWSSigner getSigner() {
        return this.signer;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setVerifier(JWSVerifier verifier) {
        this.verifier = verifier;
    }

    public void setSigner(JWSSigner signer) {
        this.signer = signer;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof JwtUtils)) return false;
        final JwtUtils other = (JwtUtils) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$secret = this.getSecret();
        final Object other$secret = other.getSecret();
        if (this$secret == null ? other$secret != null : !this$secret.equals(other$secret)) return false;
        final Object this$algorithm = this.getAlgorithm();
        final Object other$algorithm = other.getAlgorithm();
        if (this$algorithm == null ? other$algorithm != null : !this$algorithm.equals(other$algorithm)) return false;
        final Object this$verifier = this.getVerifier();
        final Object other$verifier = other.getVerifier();
        if (this$verifier == null ? other$verifier != null : !this$verifier.equals(other$verifier)) return false;
        final Object this$signer = this.getSigner();
        final Object other$signer = other.getSigner();
        if (this$signer == null ? other$signer != null : !this$signer.equals(other$signer)) return false;
        final Object this$objectMapper = this.getObjectMapper();
        final Object other$objectMapper = other.getObjectMapper();
        if (this$objectMapper == null ? other$objectMapper != null : !this$objectMapper.equals(other$objectMapper))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof JwtUtils;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $secret = this.getSecret();
        result = result * PRIME + ($secret == null ? 43 : $secret.hashCode());
        final Object $algorithm = this.getAlgorithm();
        result = result * PRIME + ($algorithm == null ? 43 : $algorithm.hashCode());
        final Object $verifier = this.getVerifier();
        result = result * PRIME + ($verifier == null ? 43 : $verifier.hashCode());
        final Object $signer = this.getSigner();
        result = result * PRIME + ($signer == null ? 43 : $signer.hashCode());
        final Object $objectMapper = this.getObjectMapper();
        result = result * PRIME + ($objectMapper == null ? 43 : $objectMapper.hashCode());
        return result;
    }

    public String toString() {
        return "JwtUtils(secret=" + this.getSecret() + ", algorithm=" + this.getAlgorithm() + ", verifier=" + this.getVerifier() + ", signer=" + this.getSigner() + ", objectMapper=" + this.getObjectMapper() + ")";
    }
}
