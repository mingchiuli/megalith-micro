package org.chiu.micro.auth.convertor;


import org.chiu.micro.auth.dto.MenuDisplayDto;
import org.chiu.micro.auth.dto.MenuWithChildDto;

import java.util.List;

public class MenuWithChildDtoConvertor {

    private MenuWithChildDtoConvertor() {
    }

    public static List<MenuWithChildDto> convert(List<MenuDisplayDto> displayDtos) {
        return displayDtos.stream()
                .map(item -> MenuWithChildDto.builder()
                        .menuId(item.menuId())
                        .icon(item.icon())
                        .name(item.name())
                        .title(item.title())
                        .status(item.status())
                        .component(item.component())
                        .url(item.url())
                        .parentId(item.parentId())
                        .orderNum(item.orderNum())
                        .type(item.type())
                        .children(convert(item.children()))
                        .build())
                .toList();
    }

    public static MenuWithChildDto convert(MenuDisplayDto displayDto) {
        return MenuWithChildDto.builder()
                .menuId(displayDto.menuId())
                .icon(displayDto.icon())
                .name(displayDto.name())
                .status(displayDto.status())
                .title(displayDto.title())
                .url(displayDto.url())
                .parentId(displayDto.parentId())
                .orderNum(displayDto.orderNum())
                .type(displayDto.type())
                .component(displayDto.component())
                .children(convert(displayDto.children()))
                .build();
    }

}
