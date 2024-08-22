package org.chiu.micro.auth.service.impl;


import org.chiu.micro.auth.convertor.MenusAndButtonsVoConvertor;
import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.service.AuthMenuService;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class AuthMenuServiceImpl implements AuthMenuService {

    private final AuthWrapper authWrapper;

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
  
}
