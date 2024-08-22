package org.chiu.micro.user.convertor;

import org.chiu.micro.user.dto.MenuDto;
import org.chiu.micro.user.entity.MenuEntity;

import java.util.List;

public class MenuDtoConvertor {

  private MenuDtoConvertor() {
  }

  public static List<MenuDto> convert(List<MenuEntity> menus) {
    return menus.stream()
        .map(menu -> MenuDto.builder()
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
