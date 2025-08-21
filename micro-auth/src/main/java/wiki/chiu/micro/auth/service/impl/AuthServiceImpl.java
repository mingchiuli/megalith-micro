package wiki.chiu.micro.auth.service.impl;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.chiu.micro.auth.convertor.ButtonVoConvertor;
import wiki.chiu.micro.auth.convertor.MenuDisplayDtoConvertor;
import wiki.chiu.micro.auth.convertor.MenuWithChildDtoConvertor;
import wiki.chiu.micro.auth.convertor.MenusAndButtonsVoConvertor;
import wiki.chiu.micro.auth.dto.*;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.token.Claims;
import wiki.chiu.micro.auth.token.TokenUtils;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.exception.AuthException;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static wiki.chiu.micro.common.lang.Const.*;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthWrapper authWrapper;

    private final RedissonClient redissonClient;

    private final ExecutorService taskExecutor;

    private final ResourceLoader resourceLoader;

    private final TokenUtils<Claims> tokenUtils;

    private String script;

    public AuthServiceImpl(AuthWrapper authWrapper, RedissonClient redissonClient, ExecutorService taskExecutor, ResourceLoader resourceLoader, TokenUtils<Claims> tokenUtils) {
        this.authWrapper = authWrapper;
        this.redissonClient = redissonClient;
        this.taskExecutor = taskExecutor;
        this.resourceLoader = resourceLoader;
        this.tokenUtils = tokenUtils;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/multi-pfadd.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public MenusAndButtonsVo getCurrentUserNav(List<String> roles) {
        List<MenuDto> menus = new ArrayList<>();
        List<ButtonDto> buttons = new ArrayList<>();

        roles.stream()
                .map(authWrapper::getCurrentUserNav)
                .forEach(partDto -> {
                    menus.addAll(partDto.menus());
                    buttons.addAll(partDto.buttons());
                });

        return MenusAndButtonsVoConvertor.convert(
                ButtonVoConvertor.convert(buttons),
                MenuWithChildDtoConvertor.convert(
                        MenuDisplayDtoConvertor.buildTreeMenu(
                                MenuDisplayDtoConvertor.convert(menus)
                        )
                )
        );
    }

    @Override
    public AuthorityRouteRpcVo findRoute(AuthorityRouteReq req) {
        recordIp(req.ipAddr());
        List<AuthorityRpcVo> systemAuthorities = authWrapper.getAllSystemAuthorities();
        for (AuthorityRpcVo dto : systemAuthorities) {
            if (routeMatch(dto.routePattern(), dto.methodType(), req.routeMapping(), req.method())) {
                return AuthorityRouteRpcVo.builder()
                        .serviceHost(dto.serviceHost())
                        .servicePort(dto.servicePort())
                        .build();
            }
        }
        throw new MissException(ExceptionMessage.NO_AUTH);
    }

    private void recordIp(String ipAddr) {
        if (StringUtils.hasLength(ipAddr)) {
            log.info("Record visit IP: {}", ipAddr);
            // Use Lua script to atomically increment visit counts for different time periods
            taskExecutor.execute(() -> redissonClient.getScript().eval(Mode.READ_WRITE, script, ReturnType.VALUE, List.of(DAY_VISIT, WEEK_VISIT, MONTH_VISIT, YEAR_VISIT), ipAddr, ipAddr, ipAddr, ipAddr));
        }
    }

    private boolean routeMatch(String routePattern, String targetMethod, String routeMapping, String method) {
        if (!Objects.equals(targetMethod, method)) {
            return false;
        }

        if (Objects.equals(routePattern, routeMapping)) {
            return true;
        }

        if (routePattern.endsWith("/**")) {
            String prefix = routePattern.replace("/**", "");

            return routeMapping.startsWith(prefix);
        }

        if (routePattern.endsWith("/*")) {
            String prefix = routePattern.replace("/*", "");

            return routeMapping.startsWith(prefix) && routeMapping.lastIndexOf("/", prefix.length()) == routeMapping.indexOf("/", prefix.length());
        }

        return false;
    }

    @Override
    public AuthRpcVo getAuthVo(String token) {
        if (!StringUtils.hasLength(token)) {
            return AuthRpcVo.builder()
                    .userId(0L)
                    .roles(Collections.emptyList())
                    .authorities(Collections.emptyList())
                    .build();
        }

        Claims claims;
        try {
            claims = tokenUtils.getVerifierByToken(token.substring(TOKEN_PREFIX.length()));
        } catch (AuthException e) {
            throw new MissException(e.getMessage());
        }
        List<String> rawRoles = getRawRoleCodes(claims.roles());
        List<String> authorities = getAuthorities(rawRoles);

        return AuthRpcVo.builder()
                .userId(Long.parseLong(claims.userId()))
                .roles(rawRoles)
                .authorities(authorities)
                .build();
    }

    @Override
    public Boolean routeCheck(AuthorityRouteCheckReq req, String token) {
        Set<String> authorities;
        try {
            authorities = getAuthAuthority(token);
        } catch (AuthException e) {
            return false;
        }

        List<AuthorityRpcVo> systemAuthorities = authWrapper.getAllSystemAuthorities();
        for (AuthorityRpcVo dto : systemAuthorities) {
            if (routeMatch(dto.routePattern(), dto.methodType(), req.routeMapping(), req.method())) {
                return authorities.contains(dto.code());
            }
        }
        return false;
    }

    private Set<String> getAuthAuthority(String token) throws AuthException {

        Set<String> authorities = authWrapper.getAllSystemAuthorities().stream()
                .filter(item -> AuthTypeEnum.WHITE_LIST.getCode().equals(item.type()))
                .map(AuthorityRpcVo::code)
                .collect(Collectors.toSet());

        if (!StringUtils.hasLength(token)) {
            return authorities;
        }

        Claims claims = tokenUtils.getVerifierByToken(token.substring(TOKEN_PREFIX.length()));
        if (redissonClient.getBucket(BLOCK_USER + claims.userId()).isExists()) {
            return authorities;
        }

        List<String> rawRoles = getRawRoleCodes(claims.roles());
        Set<String> authList = rawRoles.stream()
                .map(authWrapper::getAuthoritiesByRoleCode)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        authorities.addAll(authList);
        return authorities;
    }

    private List<String> getRawRoleCodes(List<String> roles) {
        return roles.stream()
                .map(role -> role.substring(ROLE_PREFIX.length()))
                .toList();
    }

    private List<String> getAuthorities(List<String> rawRoles) {

        return rawRoles.stream()
                .map(authWrapper::getAuthoritiesByRoleCode)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

}
