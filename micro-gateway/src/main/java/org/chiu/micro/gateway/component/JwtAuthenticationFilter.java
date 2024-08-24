package org.chiu.micro.gateway.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.chiu.micro.gateway.dto.AuthDto;
import org.chiu.micro.gateway.exception.AuthException;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws IOException, ServletException {

        String jwt = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasLength(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication;

        try {
            AuthDto authDto  = authHttpServiceWrapper.getAuthentication(jwt);
            authentication = new PreAuthenticatedAuthenticationToken(authDto.getUserId(), null, AuthorityUtils.createAuthorityList(authDto.getAuthorities()));
        } catch (AuthException e) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Result.fail(e.getMessage())));
            return;
        }

        // 非白名单资源、接口都要走这个流程 没有set就不能访问
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
