package org.chiu.micro.auth.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.lang.Const;
import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.token.TokenUtils;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import static org.chiu.micro.auth.lang.Const.*;
import static org.chiu.micro.auth.lang.ExceptionMessage.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuthenticationUtils {

    private final AuthWrapper authWrapper;

    private final TokenUtils<Claims> tokenUtils;

    private final RedissonClient redissonClient;

    private List<String> getRawRoleCodes(List<String> roles) {
        return roles.stream()
                .map(role -> role.substring(ROLE_PREFIX.getInfo().length()))
                .toList();
    }

    private List<String> getAuthorities(Long userId, List<String> rawRoles) throws AuthException {
        boolean mark = redissonClient.getBucket(BLOCK_USER.getInfo() + userId).isExists();

        if (mark) {
            throw new AuthException(RE_LOGIN.getMsg());
        }

        List<String> authorities = new ArrayList<>();
        rawRoles.forEach(role -> authorities.addAll(authWrapper.getAuthoritiesByRoleCode(role)));
        return authorities.stream()
                .distinct()
                .toList();
    }

    public AuthDto getAuthDto(String token) throws AuthException {
        long userId;
        List<String> rawRoles;
        List<String> authorities;

        if (!StringUtils.hasLength(token)) {
            userId = 0L;
            rawRoles = Collections.emptyList();
            authorities = Collections.emptyList();
        } else {
            String jwt = token.substring(TOKEN_PREFIX.getInfo().length());
            Claims claims = tokenUtils.getVerifierByToken(jwt);
            userId = Long.parseLong(claims.getUserId());
            List<String> roles = claims.getRoles();
            rawRoles = getRawRoleCodes(roles);
            authorities = getAuthorities(userId, rawRoles);
        }

        return AuthDto.builder()
                .userId(userId)
                .roles(rawRoles)
                .authorities(authorities)
                .build();
    }

    public List<String> getAuthAuthority(String token) throws AuthException {
        List<AuthorityDto> allAuthorities = authWrapper.getAllSystemAuthorities();
        List<String> whiteList = allAuthorities.stream()
                .map(AuthorityDto::getCode)
                .filter(code -> code.startsWith(Const.WHITELIST.getInfo()))
                .toList();

        List<String> authorities = new ArrayList<>(whiteList);

        if (!StringUtils.hasLength(token)) {
            return authorities;
        }

        String jwt = token.substring(TOKEN_PREFIX.getInfo().length());
        Claims claims = tokenUtils.getVerifierByToken(jwt);
        List<String> roles = claims.getRoles();
        List<String> rawRoles = getRawRoleCodes(roles);

        List<String> authList = rawRoles.stream()
                .map(authWrapper::getAuthoritiesByRoleCode)
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        authorities.addAll(authList);
        return authorities;
    }

    public boolean routeMatch(String routePattern, String targetMethod, String routeMapping, String method) {
        if (!Objects.equals(targetMethod, method)) {
            return false;
        }

        if (Objects.equals(routePattern, routeMapping)) {
            return true;
        }

        if (routePattern.endsWith("/**")) {
            String prefix = routePattern.replace("/**", "");

            if (routeMapping.startsWith(prefix)) {
                return true;
            }
        }

        if (routePattern.endsWith("/*")) {
            String prefix = routePattern.replace("/*", "");

            return routeMapping.startsWith(prefix) && routeMapping.lastIndexOf("/", prefix.length()) == routeMapping.indexOf("/", prefix.length());
        }

        return false;
    }

}
