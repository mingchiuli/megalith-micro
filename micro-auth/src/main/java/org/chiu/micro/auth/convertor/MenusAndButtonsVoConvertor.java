package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.vo.ButtonVo;
import org.chiu.micro.auth.vo.MenuWithChildVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;

public class MenusAndButtonsVoConvertor {

    private MenusAndButtonsVoConvertor() {
    }

    public static MenusAndButtonsVo convert(List<ButtonDto> buttonDtos, List<MenuWithChildDto> menuDtos) {
        List<ButtonVo> buttonVos = buttonDtos.stream()
                .distinct()
                .map(button -> ButtonVo.builder()
                        .menuId(button.menuId())
                        .parentId(button.parentId())
                        .icon(button.icon())
                        .url(button.url())
                        .title(button.title())
                        .name(button.name())
                        .component(button.component())
                        .type(button.type())
                        .orderNum(button.orderNum())
                        .status(button.status())
                        .build())
                .toList();

        List<MenuWithChildVo> menuVos = menuDtos.stream()
                .distinct()
                .map(menu -> MenuWithChildVo.builder()
                        .menuId(menu.menuId())
                        .parentId(menu.parentId())
                        .icon(menu.icon())
                        .url(menu.url())
                        .title(menu.title())
                        .name(menu.name())
                        .component(menu.component())
                        .type(menu.type())
                        .orderNum(menu.orderNum())
                        .status(menu.status())
                        .children(MenuWithChildVoConvertor.convert(menu.children()))
                        .build())
                .toList();

        return MenusAndButtonsVo.builder()
                .buttons(buttonVos)
                .menus(menuVos)
                .build();
    }
}
