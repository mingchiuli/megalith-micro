package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;
import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.dto.MenusAndButtonsRpcDto;

import java.util.List;

public class MenusAndButtonsDtoConvertor {

    private MenusAndButtonsDtoConvertor() {
    }

    public static MenusAndButtonsDto convert(MenusAndButtonsRpcDto dto) {

        List<ButtonDto> buttonDtos = dto.getButtons().stream()
                .map(button -> ButtonDto.builder()
                        .menuId(button.getMenuId())
                        .parentId(button.getParentId())
                        .icon(button.getIcon())
                        .url(button.getUrl())
                        .title(button.getTitle())
                        .name(button.getName())
                        .component(button.getComponent())
                        .type(button.getType())
                        .orderNum(button.getOrderNum())
                        .status(button.getStatus())
                        .build())
                .toList();

        List<MenuWithChildDto> menuDtos = dto.getMenus().stream()
                .map(menu -> MenuWithChildDto.builder()
                        .menuId(menu.getMenuId())
                        .parentId(menu.getParentId())
                        .icon(menu.getIcon())
                        .url(menu.getUrl())
                        .title(menu.getTitle())
                        .name(menu.getName())
                        .component(menu.getComponent())
                        .type(menu.getType())
                        .orderNum(menu.getOrderNum())
                        .status(menu.getStatus())
                        .children(MenuWithChildDtoConvertor.convert(menu.getChildren()))
                        .build())
                .toList();

        return MenusAndButtonsDto.builder()
                .buttons(buttonDtos)
                .menus(menuDtos)
                .build();

    }

}
