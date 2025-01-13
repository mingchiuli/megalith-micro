package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.vo.MenuVo;

import java.util.List;

public class MenuVoConvertor {

    private MenuVoConvertor() {
    }

    public static List<MenuVo> convert(List<MenuEntity> menus) {
        return menus.stream()
                .map(menu -> MenuVo.builder()
                        .id(menu.getId())
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
