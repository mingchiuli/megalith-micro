package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.vo.MenuVo;

import java.util.List;

public class MenuVoConvertor {

    private MenuVoConvertor() {
    }

    public static List<MenuVo> convert(List<MenuEntity> menus) {
        return menus.stream()
                .map(menu -> MenuVo.builder()
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
}
