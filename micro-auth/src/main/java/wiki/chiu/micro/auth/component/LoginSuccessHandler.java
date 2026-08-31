package wiki.chiu.micro.auth.component;

import static wiki.chiu.micro.common.lang.Const.PASSWORD_KEY;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.adapter.out.http.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.AccessTokenCookieManager;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.token.RefreshTokenCookieManager;
import wiki.chiu.micro.auth.user.LoginUser;
import wiki.chiu.micro.common.lang.Result;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JsonMapper jsonMapper;

    private final JwtTokenService jwtTokenService;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final RedissonClient redissonClient;

    private final RefreshTokenCookieManager refreshTokenCookieManager;

    private final AccessTokenCookieManager accessTokenCookieManager;

    public LoginSuccessHandler(
        JsonMapper jsonMapper,
        JwtTokenService jwtTokenService,
        UserHttpServiceWrapper userHttpServiceWrapper,
        RedissonClient redissonClient,
        RefreshTokenCookieManager refreshTokenCookieManager,
        AccessTokenCookieManager accessTokenCookieManager) {
        this.jsonMapper = jsonMapper;
        this.jwtTokenService = jwtTokenService;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.redissonClient = redissonClient;
        this.refreshTokenCookieManager = refreshTokenCookieManager;
        this.accessTokenCookieManager = accessTokenCookieManager;
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
        String username = authentication.getName();
        LoginUser user = (LoginUser) authentication.getPrincipal();
        if (user == null) {
            outputStream.write(
                jsonMapper.writeValueAsString(Result.fail("用户不存在")).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            return;
        }
        Long userId = user.getUserId();

        redissonClient.getKeys().delete(PASSWORD_KEY + userId);

        userHttpServiceWrapper.updateLoginTime(username);
        String accessToken = jwtTokenService.issueAccessToken(userId);
        String refreshToken = jwtTokenService.issueRefreshToken(userId);
        response.addHeader(
            HttpHeaders.SET_COOKIE, accessTokenCookieManager.create(accessToken).toString());
        response.addHeader(
            HttpHeaders.SET_COOKIE, refreshTokenCookieManager.create(refreshToken).toString());

        outputStream.write(
            jsonMapper.writeValueAsString(Result.success()).getBytes(StandardCharsets.UTF_8));

        outputStream.flush();
        outputStream.close();
    }
}
