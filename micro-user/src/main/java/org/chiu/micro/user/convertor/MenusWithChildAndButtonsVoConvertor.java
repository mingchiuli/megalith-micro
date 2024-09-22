package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.MenusWithChildAndButtonsDto;
import org.chiu.micro.user.vo.ButtonVo;
import org.chiu.micro.user.vo.MenuWithChildVo;
import org.chiu.micro.user.vo.MenusAndButtonsVo;

import java.util.List;

public class MenusWithChildAndButtonsVoConvertor {

    private MenusWithChildAndButtonsVoConvertor() {
    }

    public static MenusAndButtonsVo convert(MenusWithChildAndButtonsDto dto) {

        List<ButtonVo> buttonVos = dto.getButtons().stream()
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

        List<MenuWithChildVo> menuVos = dto.getMenus().stream()
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
