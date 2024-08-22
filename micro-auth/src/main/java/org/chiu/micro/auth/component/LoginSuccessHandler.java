package org.chiu.micro.auth.component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.token.TokenUtils;
import org.chiu.micro.auth.user.LoginUser;
import org.chiu.micro.auth.vo.LoginSuccessVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import static org.chiu.micro.auth.lang.Const.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

		private final ObjectMapper objectMapper;

		private final TokenUtils<Claims> tokenUtils;

		private final UserHttpServiceWrapper userHttpServiceWrapper;

		private final StringRedisTemplate redisTemplate;

		@Value("${blog.jwt.access-token-expire}")
		private long accessExpire;

		@Value("${blog.jwt.refresh-token-expire}")
		private long refreshExpire;

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			ServletOutputStream outputStream = response.getOutputStream();
			String username = authentication.getName();
			LoginUser user = (LoginUser) authentication.getPrincipal();
			Long userId = user.getUserId();

			List<String> keys = List.of(PASSWORD_KEY.getInfo() + username, BLOCK_USER.getInfo() + userId);
			redisTemplate.delete(keys);

			userHttpServiceWrapper.updateLoginTime(username);
			// 生成jwt
			String accessToken = tokenUtils.generateToken(userId.toString(),
					authentication.getAuthorities().stream()
							.map(GrantedAuthority::getAuthority)
							.toList(),
					accessExpire);

			String refreshToken = tokenUtils.generateToken(userId.toString(),
					Collections.singletonList("REFRESH_TOKEN"),
					refreshExpire);

			outputStream.write(
					objectMapper.writeValueAsString(
									Result.success(
											LoginSuccessVo.builder()
													.accessToken(TOKEN_PREFIX.getInfo() + accessToken)
													.refreshToken(TOKEN_PREFIX.getInfo() + refreshToken)
													.build())
							)
							.getBytes(StandardCharsets.UTF_8)
			);

			outputStream.flush();
			outputStream.close();
		}

}
