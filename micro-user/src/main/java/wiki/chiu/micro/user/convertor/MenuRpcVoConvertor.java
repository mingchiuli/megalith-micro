package wiki.chiu.micro.user.convertor;

import java.util.List;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.entity.MenuEntity;

public class MenuRpcVoConvertor {

  private MenuRpcVoConvertor() {}

  public static List<MenuRpcVo> convert(List<MenuEntity> menus) {
    return menus.stream()
        .map(
            menu ->
                MenuRpcVo.builder()
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
