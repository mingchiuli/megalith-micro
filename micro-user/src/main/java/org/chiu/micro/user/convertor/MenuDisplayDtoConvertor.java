package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.MenuDisplayDto;
import org.chiu.micro.user.dto.MenuDto;
import org.chiu.micro.user.lang.StatusEnum;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MenuDisplayDtoConvertor {

    private MenuDisplayDtoConvertor() {
    }

    public static List<MenuDisplayDto> convert(List<MenuDto> menus, boolean statusCheck) {
        Stream<MenuDto> menuStream = menus.stream();
        if (Boolean.TRUE.equals(statusCheck)) {
            menuStream = menuStream.filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.getStatus()));
        }

        return menuStream
                .map(menu -> MenuDisplayDto.builder()
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
                        .build())
                .toList();
    }

    public static List<MenuDisplayDto> buildTreeMenu(List<MenuDisplayDto> menus) {
        //2.组装父子的树形结构
        //2.1 找到所有一级分类
        return menus.stream()
                .filter(menu -> menu.getParentId() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu, menus));
                    return menu;
                })
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.getOrderNum()) ? 0 : menu.getOrderNum()))
                .toList();
    }

    private static List<MenuDisplayDto> getChildren(MenuDisplayDto root, List<MenuDisplayDto> all) {
        return all.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), root.getMenuId()))
                .map(menu -> {
                    menu.setChildren(getChildren(menu, all));
                    return menu;
                })
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.getOrderNum()) ? 0 : menu.getOrderNum()))
                .toList();
    }
}
