package org.chiu.micro.gateway.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.chiu.micro.gateway.lang.Result;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 如果访问白名单没有配置的url
 * 和SecurityContextHolder.getContext().setAuthentication(token);
 * 就会跳转到这个处理逻辑
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

		private final ObjectMapper objectMapper;

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				ServletOutputStream outputStream = response.getOutputStream();
				Result<String> result = Result.fail(authException.getMessage());
				outputStream.write(
						objectMapper.writeValueAsString(result)
								.getBytes(StandardCharsets.UTF_8)
				);

				outputStream.flush();
				outputStream.close();
	}
}
