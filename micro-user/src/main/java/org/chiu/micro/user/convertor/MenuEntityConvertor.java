package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.req.MenuEntityReq;

public class MenuEntityConvertor {

    private MenuEntityConvertor() {
    }

    public static MenuEntity convert(MenuEntityReq menu) {
        return MenuEntity.builder()
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
                .build();
    }
}
