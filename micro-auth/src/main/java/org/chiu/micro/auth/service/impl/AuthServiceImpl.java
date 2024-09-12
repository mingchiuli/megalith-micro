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
import org.chiu.micro.auth.exception.AuthException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthWrapper authWrapper;

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final SecurityAuthenticationUtils securityAuthenticationUtils;

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
    public AuthorityRouteVo route(AuthorityRouteReq req) throws AuthException {
        String token = req.getToken();
        List<String> authorities = securityAuthenticationUtils.getAuthAuthority(token);
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
