package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.MenusWithChildAndButtonsDto;
import org.chiu.micro.user.vo.ButtonVo;
import org.chiu.micro.user.vo.MenuWithChildVo;
import org.chiu.micro.user.vo.MenusAndButtonsVo;

import java.util.List;

public class MenusWithChildAndButtonsVoConvertor {

    private MenusWithChildAndButtonsVoConvertor() {
    }

    public static MenusAndButtonsVo convert(MenusWithChildAndButtonsDto dto) {

        List<ButtonVo> buttonVos = dto.buttons().stream()
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

        List<MenuWithChildVo> menuVos = dto.menus().stream()
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
