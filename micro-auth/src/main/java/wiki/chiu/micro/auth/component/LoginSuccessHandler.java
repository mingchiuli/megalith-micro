package wiki.chiu.micro.auth.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.Claims;
import wiki.chiu.micro.auth.token.TokenUtils;
import wiki.chiu.micro.auth.user.LoginUser;
import wiki.chiu.micro.auth.vo.LoginSuccessVo;
import wiki.chiu.micro.common.lang.Result;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static wiki.chiu.micro.common.lang.Const.*;


@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final TokenUtils<Claims> tokenUtils;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final RedissonClient redissonClient;

    @Value("${megalith.blog.jwt.access-token-expire}")
    private long accessExpire;

    @Value("${megalith.blog.jwt.refresh-token-expire}")
    private long refreshExpire;

    public LoginSuccessHandler(ObjectMapper objectMapper, TokenUtils<Claims> tokenUtils, UserHttpServiceWrapper userHttpServiceWrapper, RedissonClient redissonClient) {
        this.objectMapper = objectMapper;
        this.tokenUtils = tokenUtils;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.redissonClient = redissonClient;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        String username = authentication.getName();
        LoginUser user = (LoginUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        List<String> keys = List.of(PASSWORD_KEY + username, BLOCK_USER + userId);
        redissonClient.getKeys().delete(keys.toArray(new String[0]));

        userHttpServiceWrapper.updateLoginTime(username);
        // 生成jwt
        String accessToken = tokenUtils.generateToken(userId.toString(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                accessExpire);

        String refreshToken = tokenUtils.generateToken(userId.toString(),
                Collections.singletonList("refresh"),
                refreshExpire);

        outputStream.write(
                objectMapper.writeValueAsString(
                                Result.success(
                                        LoginSuccessVo.builder()
                                                .accessToken(TOKEN_PREFIX + accessToken)
                                                .refreshToken(TOKEN_PREFIX + refreshToken)
                                                .build())
                        )
                        .getBytes(StandardCharsets.UTF_8)
        );

        outputStream.flush();
        outputStream.close();
    }

}
