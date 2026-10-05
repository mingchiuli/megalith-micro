package wiki.chiu.micro.auth.adapter.in.security;

import static wiki.chiu.micro.common.error.ExceptionMessage.INVALID_LOGIN_OPERATE;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.adapter.in.security.token.EmailAuthenticationToken;
import wiki.chiu.micro.auth.adapter.in.security.token.SMSAuthenticationToken;

final class LoginAuthenticationConverter {

    private final JsonMapper jsonMapper;

    LoginAuthenticationConverter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    Authentication convert(HttpServletRequest request) {
        if (!isJson(request)) {
            throw invalidLogin(null);
        }

        LoginRequest loginRequest;
        try {
            loginRequest = jsonMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException | JacksonException e) {
            throw invalidLogin(e);
        }

        if (loginRequest == null
            || loginRequest.loginType() == null
            || isBlank(loginRequest.principal())
            || isBlank(loginRequest.credential())) {
            throw invalidLogin(null);
        }

        return switch (loginRequest.loginType()) {
            case PASSWORD -> UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.principal(), loginRequest.credential());
            case EMAIL ->
                new EmailAuthenticationToken(loginRequest.principal(), loginRequest.credential());
            case SMS ->
                new SMSAuthenticationToken(loginRequest.principal(), loginRequest.credential());
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isJson(HttpServletRequest request) {
        try {
            return request.getContentType() != null
                && MediaType.APPLICATION_JSON.isCompatibleWith(
                MediaType.parseMediaType(request.getContentType()));
        } catch (InvalidMediaTypeException exception) {
            return false;
        }
    }

    private BadCredentialsException invalidLogin(Exception cause) {
        return new BadCredentialsException(INVALID_LOGIN_OPERATE.getMsg(), cause);
    }
}
