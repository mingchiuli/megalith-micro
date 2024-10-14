package org.chiu.micro.auth.service.impl;


import jakarta.annotation.PostConstruct;
import org.chiu.micro.auth.convertor.ButtonVoConvertor;
import org.chiu.micro.auth.convertor.MenuDisplayDtoConvertor;
import org.chiu.micro.auth.convertor.MenuWithChildDtoConvertor;
import org.chiu.micro.auth.convertor.MenusAndButtonsVoConvertor;
import org.chiu.micro.auth.dto.*;
import org.chiu.micro.auth.exception.AuthException;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.token.Claims;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.chiu.micro.auth.lang.Const.*;


@Service
public class AuthServiceImpl implements AuthService {

    private final AuthWrapper authWrapper;

    private final SecurityAuthenticationUtils securityAuthenticationUtils;

    private final RedissonClient redissonClient;

    private final ExecutorService taskExecutor;

    private final ResourceLoader resourceLoader;

    private String script;

    public AuthServiceImpl(AuthWrapper authWrapper, SecurityAuthenticationUtils securityAuthenticationUtils, RedissonClient redissonClient, @Qualifier("commonExecutor") ExecutorService taskExecutor, ResourceLoader resourceLoader) {
        this.authWrapper = authWrapper;
        this.securityAuthenticationUtils = securityAuthenticationUtils;
        this.redissonClient = redissonClient;
        this.taskExecutor = taskExecutor;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/statistics.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public MenusAndButtonsVo getCurrentUserNav(List<String> roles) {
        List<MenuDto> menus = new ArrayList<>();
        List<ButtonDto> buttons = new ArrayList<>();

        roles.forEach(role -> {
            MenusAndButtonsDto partDto = authWrapper.getCurrentUserNav(role);
            menus.addAll(partDto.menus());
            buttons.addAll(partDto.buttons());
        });

        List<MenuDisplayDto> menuEntities = MenuDisplayDtoConvertor.convert(menus);
        List<MenuDisplayDto> displayDtos = MenuDisplayDtoConvertor.buildTreeMenu(menuEntities);
        List<MenuWithChildDto> menuDtos = MenuWithChildDtoConvertor.convert(displayDtos);
        List<ButtonDto> buttonDtos = ButtonVoConvertor.convert(buttons);

        return MenusAndButtonsVoConvertor.convert(buttonDtos, menuDtos);
    }

    @Override
    public AuthorityRouteVo route(AuthorityRouteReq req, String token) {
        //record ip
        String ipAddr = req.ipAddr();
        if (StringUtils.hasLength(ipAddr)) {
            taskExecutor.execute(() -> redissonClient.getScript().eval(Mode.READ_WRITE, script, ReturnType.VALUE, List.of(DAY_VISIT.getInfo(), WEEK_VISIT.getInfo(), MONTH_VISIT.getInfo(), YEAR_VISIT.getInfo()), ipAddr));
        }

        List<String> authorities;
        try {
            authorities = securityAuthenticationUtils.getAuthAuthority(token);
        } catch (AuthException e) {
            return AuthorityRouteVo.builder()
                    .auth(false)
                    .build();
        }

        List<AuthorityDto> systemAuthorities = authWrapper.getAllSystemAuthorities();
        for (AuthorityDto dto : systemAuthorities) {
            if (securityAuthenticationUtils.routeMatch(dto.routePattern(), dto.methodType(), req.routeMapping(), req.method())) {
                if (authorities.contains(dto.code())) {
                    return AuthorityRouteVo.builder()
                            .auth(true)
                            .serviceHost(dto.serviceHost())
                            .servicePort(dto.servicePort())
                            .build();
                }
                return AuthorityRouteVo.builder()
                        .auth(false)
                        .build();
            }
        }
        return AuthorityRouteVo.builder()
                .auth(false)
                .build();
    }

    @Override
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
            Claims claims = securityAuthenticationUtils.getVerifierByToken(jwt);
            userId = Long.parseLong(claims.userId());
            List<String> roles = claims.roles();
            rawRoles = securityAuthenticationUtils.getRawRoleCodes(roles);
            authorities = securityAuthenticationUtils.getAuthorities(userId, rawRoles);
        }

        return AuthDto.builder()
                .userId(userId)
                .roles(rawRoles)
                .authorities(authorities)
                .build();
    }

}
