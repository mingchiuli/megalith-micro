package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.MenuWithChildDto;
import org.chiu.micro.user.vo.MenuWithChildVo;

import java.util.List;

public class MenuWithChildVoConvertor {

    private MenuWithChildVoConvertor() {
    }

    public static List<MenuWithChildVo> convert(List<MenuWithChildDto> menuVos) {
        return menuVos.stream()
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
                        .children(convert(menu.children()))
                        .build())
                .toList();
    }
}
