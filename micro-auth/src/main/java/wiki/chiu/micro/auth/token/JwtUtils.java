package wiki.chiu.micro.auth.token;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.exception.MissException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static wiki.chiu.micro.common.lang.Const.ROLE_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.AUTH_EXCEPTION;
import static wiki.chiu.micro.common.lang.ExceptionMessage.TOKEN_INVALID;

/**
 * jwt工具类
 */
@Component
@ConfigurationProperties(prefix = "megalith.blog.jwt")
public class JwtUtils implements TokenUtils<Claims> {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);
    private String secret;

    private JWSVerifier verifier;

    private JWSSigner signer;

    public void setSecret(String secret) {
        this.secret = secret;
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
                .map(role -> ROLE_PREFIX + role)
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
            log.error("token:{}", token);
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                throw new AuthException(AUTH_EXCEPTION.getMsg());
            }
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = jwtClaimsSet.getExpirationTime();
            if (new Date().after(expirationTime)) {
                throw new AuthException(TOKEN_INVALID.getMsg());
            }
            var roles = jwtClaimsSet.getClaim("roles");

            return new Claims(jwtClaimsSet.getSubject(), (List<String>) roles);
        } catch (JOSEException | ParseException e) {
            throw new AuthException(TOKEN_INVALID.getMsg());
        }
    }
}
