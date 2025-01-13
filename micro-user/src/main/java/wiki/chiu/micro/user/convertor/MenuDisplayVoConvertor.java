package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.vo.MenuDisplayVo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MenuDisplayVoConvertor {

    private MenuDisplayVoConvertor() {
    }

    public static List<MenuDisplayVo> convert(List<MenuEntity> menus) {
        return menus.stream()
                .filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.getStatus()))
                .map(menu -> MenuDisplayVo.builder()
                        .id(menu.getId())
                        .parentId(menu.getParentId())
                        .icon(menu.getIcon())
                        .url(menu.getUrl())
                        .updated(menu.getUpdated())
                        .created(menu.getCreated())
                        .title(menu.getTitle())
                        .name(menu.getName())
                        .component(menu.getComponent())
                        .type(menu.getType())
                        .orderNum(menu.getOrderNum())
                        .status(menu.getStatus())
                        .build())
                .toList();
    }

    public static List<MenuDisplayVo> buildTreeMenu(List<MenuDisplayVo> menus) {
        //2.组装父子的树形结构
        //2.1 找到所有一级分类
        return menus.stream()
                .filter(menu -> Objects.equals(menu.parentId(), 0L))
                .map(menu -> new MenuDisplayVo(menu, 0L, getChildren(menu, menus)))
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }

    private static List<MenuDisplayVo> getChildren(MenuDisplayVo root, List<MenuDisplayVo> all) {
        return all.stream()
                .filter(menu -> Objects.equals(menu.parentId(), root.id()))
                .map(menu -> new MenuDisplayVo(menu, root.id(), getChildren(menu, all)))
                .sorted(Comparator.comparingInt(menu -> Objects.isNull(menu.orderNum()) ? 0 : menu.orderNum()))
                .toList();
    }
}
