package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.MenusAndButtonsDto;
import org.chiu.micro.auth.vo.ButtonVo;
import org.chiu.micro.auth.vo.MenuWithChildVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;

public class MenusAndButtonsVoConvertor {

    private MenusAndButtonsVoConvertor() {
    }

    public static MenusAndButtonsVo convert(MenusAndButtonsDto dto) {

        List<ButtonVo> buttonVos = dto.getButtons().stream().distinct()
                .map(button -> ButtonVo.builder()
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

        List<MenuWithChildVo> menuVos = dto.getMenus().stream().distinct()
                .map(menu -> MenuWithChildVo.builder()
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
                        .children(MenuWithChildVoConvertor.convert(menu.getChildren()))
                        .build())
                .toList();

        return MenusAndButtonsVo.builder()
                .buttons(buttonVos)
                .menus(menuVos)
                .build();
    }

}
