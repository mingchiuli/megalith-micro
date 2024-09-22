package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.vo.MenuEntityVo;

public class MenuEntityVoConvertor {

    private MenuEntityVoConvertor() {
    }

    public static MenuEntityVo convert(MenuEntity menuEntity) {
        return MenuEntityVo.builder()
                .menuId(menuEntity.getMenuId())
                .url(menuEntity.getUrl())
                .title(menuEntity.getTitle())
                .type(menuEntity.getType())
                .name(menuEntity.getName())
                .component(menuEntity.getComponent())
                .orderNum(menuEntity.getOrderNum())
                .parentId(menuEntity.getParentId())
                .icon(menuEntity.getIcon())
                .status(menuEntity.getStatus())
                .build();
    }
}
