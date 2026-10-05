package wiki.chiu.micro.auth.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.adapter.in.http.SessionCookies;
import wiki.chiu.micro.auth.application.model.SessionTokens;
import wiki.chiu.micro.auth.application.port.in.LoginSession;
import wiki.chiu.micro.common.result.Result;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String USER_MISSING = "用户不存在";

    private final JsonMapper jsonMapper;

    private final LoginSession loginSession;

    private final SessionCookies sessionCookies;

    public LoginSuccessHandler(
        JsonMapper jsonMapper, LoginSession loginSession, SessionCookies sessionCookies) {
        this.jsonMapper = jsonMapper;
        this.loginSession = loginSession;
        this.sessionCookies = sessionCookies;
    }

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain,
        Authentication authentication)
        throws IOException {
        onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    public void onAuthenticationSuccess(
        @NonNull HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication)
        throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        LoginUser user = (LoginUser) authentication.getPrincipal();
        if (user == null) {
            outputStream.write(
                jsonMapper
                    .writeValueAsString(Result.fail(USER_MISSING))
                    .getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            return;
        }

        SessionTokens tokens = loginSession.start(user.getUserId(), authentication.getName());
        response.addHeader(
            HttpHeaders.SET_COOKIE, sessionCookies.access(tokens.accessToken()).toString());
        response.addHeader(
            HttpHeaders.SET_COOKIE, sessionCookies.refresh(tokens.refreshToken()).toString());

        outputStream.write(
            jsonMapper.writeValueAsString(Result.success()).getBytes(StandardCharsets.UTF_8));

        outputStream.flush();
        outputStream.close();
    }
}
