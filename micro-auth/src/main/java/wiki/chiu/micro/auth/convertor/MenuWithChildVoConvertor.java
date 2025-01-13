package wiki.chiu.micro.auth.convertor;

import java.util.List;

import wiki.chiu.micro.auth.dto.MenuWithChildDto;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;

public class MenuWithChildVoConvertor {
    private MenuWithChildVoConvertor() {
    }

    public static List<MenuWithChildVo> convert(List<MenuWithChildDto> menuVos) {
        return menuVos.stream()
                .map(menu -> MenuWithChildVo.builder()
                        .id(menu.id())
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
