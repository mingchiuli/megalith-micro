package wiki.chiu.micro.auth.convertor;


import wiki.chiu.micro.auth.dto.MenuDisplayDto;
import wiki.chiu.micro.auth.dto.MenuWithChildDto;

import java.util.List;

public class MenuWithChildDtoConvertor {

    private MenuWithChildDtoConvertor() {
    }

    public static List<MenuWithChildDto> convert(List<MenuDisplayDto> displayDtos) {
        return displayDtos.stream()
                .map(item -> MenuWithChildDto.builder()
                        .menuId(item.menuId())
                        .icon(item.icon())
                        .name(item.name())
                        .title(item.title())
                        .status(item.status())
                        .component(item.component())
                        .url(item.url())
                        .parentId(item.parentId())
                        .orderNum(item.orderNum())
                        .type(item.type())
                        .children(convert(item.children()))
                        .build())
                .toList();
    }

}
