package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.user.entity.MenuEntity;

import java.util.List;

public class MenuRpcVoConvertor {

    private MenuRpcVoConvertor() {
    }

    public static List<MenuRpcVo> convert(List<MenuEntity> menus) {
        return menus.stream()
                .map(menu -> MenuRpcVo.builder()
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
