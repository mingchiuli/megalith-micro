package wiki.chiu.micro.auth.convertor;

import wiki.chiu.micro.auth.dto.MenuDisplayDto;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MenuDisplayDtoConvertor {

    private MenuDisplayDtoConvertor() {}

    public static List<MenuDisplayDto> convert(List<MenuDto> menus) {

        return menus.stream()
                .filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.status()))
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
        MenuDisplayDto root = menus.stream()
                .filter(menu -> Objects.equals(menu.parentId(), 0L)).findFirst()
                .orElseThrow(() -> new MissException(ExceptionMessage.NO_FOUND));

        return menus.stream()
                .filter(menu -> Objects.equals(menu.parentId(), root.menuId()))
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
