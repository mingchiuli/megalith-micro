package org.chiu.micro.auth.utils;

import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.token.TokenUtils;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.chiu.micro.common.dto.AuthorityRpcDto;
import org.chiu.micro.common.exception.AuthException;
import org.chiu.micro.common.lang.AuthStatusEnum;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.chiu.micro.common.lang.Const.*;
import static org.chiu.micro.common.lang.ExceptionMessage.RE_LOGIN;


@Component
public class SecurityAuthenticationUtils {

    private final AuthWrapper authWrapper;

    private final TokenUtils<Claims> tokenUtils;

    private final RedissonClient redissonClient;

    public SecurityAuthenticationUtils(AuthWrapper authWrapper, TokenUtils<Claims> tokenUtils, RedissonClient redissonClient) {
        this.authWrapper = authWrapper;
        this.tokenUtils = tokenUtils;
        this.redissonClient = redissonClient;
    }

    public List<String> getRawRoleCodes(List<String> roles) {
        return roles.stream()
                .map(role -> role.substring(ROLE_PREFIX.length()))
                .toList();
    }

    public List<String> getAuthorities(Long userId, List<String> rawRoles) throws AuthException {
        boolean mark = redissonClient.getBucket(BLOCK_USER + userId).isExists();

        if (mark) {
            throw new AuthException(RE_LOGIN.getMsg());
        }

        List<String> authorities = new ArrayList<>();
        rawRoles.forEach(role -> authorities.addAll(authWrapper.getAuthoritiesByRoleCode(role)));
        return authorities.stream()
                .distinct()
                .toList();
    }

    public List<String> getAuthAuthority(String token) throws AuthException {
        List<AuthorityRpcDto> allAuthorities = authWrapper.getAllSystemAuthorities();
        List<String> whiteList = allAuthorities.stream()
                .filter(item -> AuthStatusEnum.WHITE_LIST.getCode().equals(item.type()))
                .map(AuthorityRpcDto::code)
                .toList();

        List<String> authorities = new ArrayList<>(whiteList);

        if (!StringUtils.hasLength(token)) {
            return authorities;
        }

        String jwt = token.substring(TOKEN_PREFIX.length());
        Claims claims = tokenUtils.getVerifierByToken(jwt);
        List<String> roles = claims.roles();
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

    public Claims getVerifierByToken(String jwt) throws AuthException {
        return tokenUtils.getVerifierByToken(jwt);
    }
}
