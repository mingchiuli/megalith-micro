package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.MenuDisplayDto;
import org.chiu.micro.auth.dto.MenuDto;
import org.chiu.micro.auth.lang.StatusEnum;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MenuDisplayDtoConvertor {

    private MenuDisplayDtoConvertor() {}

    public static List<MenuDisplayDto> convert(List<MenuDto> menus, boolean statusCheck) {
        Stream<MenuDto> menuStream = menus.stream();
        if (Boolean.TRUE.equals(statusCheck)) {
            menuStream = menuStream.filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.status()));
        }

        return menuStream
                .distinct()
                .map(menu -> MenuDisplayDto.builder()
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
    }

    public static List<MenuDisplayDto> buildTreeMenu(List<MenuDisplayDto> menus) {
        //2.组装父子的树形结构
        //2.1 找到所有一级分类
        return menus.stream()
                .filter(menu -> menu.parentId() == 0)
                .map(menu -> new MenuDisplayDto(menu, 0L, getChildren(menu, menus)))
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }

    private static List<MenuDisplayDto> getChildren(MenuDisplayDto root, List<MenuDisplayDto> all) {
        return all.stream()
                .filter(menu -> Objects.equals(menu.parentId(), root.menuId()))
                .map(menu -> new MenuDisplayDto(menu, root.menuId(), getChildren(menu, all)))
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }
}
