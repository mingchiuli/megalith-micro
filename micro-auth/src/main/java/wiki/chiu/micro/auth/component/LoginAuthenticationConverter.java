package wiki.chiu.micro.auth.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.component.token.EmailAuthenticationToken;
import wiki.chiu.micro.auth.component.token.SMSAuthenticationToken;
import wiki.chiu.micro.auth.dto.LoginRequest;

import java.io.IOException;

import static wiki.chiu.micro.common.lang.ExceptionMessage.INVALID_LOGIN_OPERATE;

@Component
public final class LoginAuthenticationConverter implements AuthenticationConverter {

    private final JsonMapper jsonMapper;

    public LoginAuthenticationConverter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        LoginRequest loginRequest;
        try {
            loginRequest = jsonMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException | JacksonException e) {
            throw invalidLogin(e);
        }

        if (loginRequest == null
                || loginRequest.loginType() == null
                || !StringUtils.hasText(loginRequest.principal())
                || !StringUtils.hasText(loginRequest.credential())) {
            throw invalidLogin(null);
        }

        return switch (loginRequest.loginType()) {
            case PASSWORD -> UsernamePasswordAuthenticationToken.unauthenticated(
                    loginRequest.principal(), loginRequest.credential());
            case EMAIL -> new EmailAuthenticationToken(
                    loginRequest.principal(), loginRequest.credential());
            case SMS -> new SMSAuthenticationToken(
                    loginRequest.principal(), loginRequest.credential());
        };
    }

    private BadCredentialsException invalidLogin(Exception cause) {
        return new BadCredentialsException(INVALID_LOGIN_OPERATE.getMsg(), cause);
    }
}
