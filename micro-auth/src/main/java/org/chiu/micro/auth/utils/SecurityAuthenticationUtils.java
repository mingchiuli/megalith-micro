package org.chiu.micro.auth.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.token.TokenUtils;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import static org.chiu.micro.auth.lang.Const.*;
import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityAuthenticationUtils {

    private final AuthWrapper authWrapper;

    private final TokenUtils<Claims> tokenUtils;

    private final StringRedisTemplate redisTemplate;


    private List<String> getRawRoleCodes(List<String> roles) {
        List<String> rawRoles = new ArrayList<>();
        roles.forEach(role -> rawRoles.add(role.substring(ROLE_PREFIX.getInfo().length())));
        return rawRoles;
    }

    private List<String> getAuthorities(Long userId, List<String> rawRoles) throws AuthException {
        boolean mark = redisTemplate.hasKey(BLOCK_USER.getInfo() + userId);

        if (mark) {
            throw new AuthException(RE_LOGIN.getMsg());
        }

        List<String> authorities = new ArrayList<>();
        rawRoles.forEach(role -> authorities.addAll(authWrapper.getAuthoritiesByRoleCode(role)));
        return authorities.stream()
                .distinct()
                .toList();
    }

    @SneakyThrows
    public AuthDto getAuthDto(String token) {
        String jwt = token.substring(TOKEN_PREFIX.getInfo().length());
        Claims claims = tokenUtils.getVerifierByToken(jwt);
        Long userId = Long.valueOf(claims.getUserId());
        List<String> roles = claims.getRoles();
        List<String> rawRoles = getRawRoleCodes(roles);
        List<String> authorities = getAuthorities(userId, rawRoles);

        return AuthDto.builder()
                .userId(userId)
                .roles(rawRoles)
                .authorities(authorities)
                .build();
    }
}
