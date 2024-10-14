package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.lang.StatusEnum;
import org.chiu.micro.user.vo.MenuDisplayVo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MenuDisplayVoConvertor {

    private MenuDisplayVoConvertor() {
    }

    public static List<MenuDisplayVo> convert(List<MenuEntity> menus) {
        return menus.stream()
                .filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.getStatus()))
                .map(menu -> MenuDisplayVo.builder()
                        .menuId(menu.getMenuId())
                        .parentId(menu.getParentId())
                        .icon(menu.getIcon())
                        .url(menu.getUrl())
                        .updated(menu.getUpdated())
                        .created(menu.getCreated())
                        .title(menu.getTitle())
                        .name(menu.getName())
                        .component(menu.getComponent())
                        .type(menu.getType())
                        .orderNum(menu.getOrderNum())
                        .status(menu.getStatus())
                        .build())
                .toList();
    }

    public static List<MenuDisplayVo> buildTreeMenu(List<MenuDisplayVo> menus) {
        //2.组装父子的树形结构
        //2.1 找到所有一级分类
        return menus.stream()
                .filter(menu -> menu.parentId() == 0)
                .map(menu -> new MenuDisplayVo(menu, 0L, getChildren(menu, menus)))
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }

    private static List<MenuDisplayVo> getChildren(MenuDisplayVo root, List<MenuDisplayVo> all) {
        return all.stream()
                .filter(menu -> Objects.equals(menu.parentId(), root.menuId()))
                .map(menu -> new MenuDisplayVo(menu, root.menuId(), getChildren(menu, all)))

                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }
}
