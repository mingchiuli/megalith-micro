package org.chiu.micro.auth.service.impl;


import org.chiu.micro.auth.convertor.AuthorityVoConvertor;
import org.chiu.micro.auth.convertor.MenusAndButtonsVoConvertor;
import org.chiu.micro.auth.dto.AuthorityDto;
import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.chiu.micro.auth.service.AuthService;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.AuthorityVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.chiu.micro.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.chiu.micro.auth.lang.Const.*;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthWrapper authWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SecurityAuthenticationUtils securityAuthenticationUtils;
    
    private final RedissonClient redissonClient;
    
    @Qualifier("commonExecutor")
    private final ExecutorService taskExecutor;

    private final ResourceLoader resourceLoader;

    private String script;
    
    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource resource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/statistics.lua");
        script = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public MenusAndButtonsVo getCurrentUserNav(List<String> roles) {
        var dto = new MenusAndButtonsDto();
        List<MenuWithChildDto> menus = new ArrayList<>();
        List<ButtonDto> buttons = new ArrayList<>();
        dto.setMenus(menus);
        dto.setButtons(buttons);
        roles.forEach(role -> {
            MenusAndButtonsDto partDto = authWrapper.getCurrentUserNav(role);
            menus.addAll(partDto.getMenus());
            buttons.addAll(partDto.getButtons());
        });

        return MenusAndButtonsVoConvertor.convert(dto);
    }

    @Override
    public List<AuthorityVo> getSystemAuthority(List<String> serviceHost) {
        List<AuthorityDto> systemAuthorities = userHttpServiceWrapper.getSystemAuthorities(serviceHost);
        return AuthorityVoConvertor.convert(systemAuthorities);
    }

    @Override
    public AuthorityRouteVo route(AuthorityRouteReq req, String token) {
        //record ip
        
        taskExecutor.execute(() -> redissonClient.getScript().eval(Mode.READ_WRITE, script, ReturnType.INTEGER, List.of(DAY_VISIT.getInfo(), WEEK_VISIT.getInfo(), MONTH_VISIT.getInfo(), YEAR_VISIT.getInfo()), req.getIpAddr()));
        
        List<String> authorities;
        try {
            authorities = securityAuthenticationUtils.getAuthAuthority(token);
        } catch(AuthException e) {
            return AuthorityRouteVo.builder()
                    .auth(false)
                    .build();
        }
        
        List<AuthorityDto> systemAuthorities = authWrapper.getAllSystemAuthorities();
        for (AuthorityDto dto : systemAuthorities) {
            if (securityAuthenticationUtils.routeMatch(dto.getRoutePattern(), dto.getMethodType(), req.getRouteMapping(), req.getMethod())) {
                if (authorities.contains(dto.getCode())) {
                    return AuthorityRouteVo.builder()
                            .auth(true)
                            .serviceHost(dto.getServiceHost())
                            .servicePort(dto.getServicePort())
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
  
}
