package wiki.chiu.micro.auth.convertor;

import java.util.List;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;

public class MenuDtoConvertor {

  private MenuDtoConvertor() {}

  public static List<MenuDto> convert(List<MenuRpcVo> menus) {
    return menus.stream()
        .distinct()
        .map(
            menu ->
                MenuDto.builder()
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
                    .build())
        .toList();
  }
}
