package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.*;

import java.util.List;

public class MenusAndButtonsDtoConvertor {

    private MenusAndButtonsDtoConvertor() {
    }

    public static MenusAndButtonsDto convert(MenusAndButtonsRpcDto dto) {

        List<ButtonDto> buttonDtos = dto.buttons().stream()
                .distinct()
                .map(button -> ButtonDto.builder()
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

        List<MenuDto> menuDtos = dto.menus().stream()
                .distinct()
                .map(menu -> MenuDto.builder()
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
                        .build())
                .toList();

        return MenusAndButtonsDto.builder()
                .buttons(buttonDtos)
                .menus(menuDtos)
                .build();

    }

}
