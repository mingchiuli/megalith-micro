package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.req.MenuEntityReq;


public class MenuEntityConvertor {

    private MenuEntityConvertor() {
    }

    public static MenuEntity convert(MenuEntityReq menu) {
        return MenuEntity.builder()
                .id(menu.id().orElse(null))
                .parentId(menu.parentId())
                .icon(menu.icon())
                .url(menu.url())
                .title(menu.title())
                .name(menu.name())
                .component(menu.component())
                .type(menu.type())
                .orderNum(menu.orderNum())
                .status(menu.status())
                .build();
    }

    public static MenuEntity convert(MenuEntityReq menu, MenuEntity dealMenu) {
        MenuEntity menuEntity = new MenuEntity();

        menuEntity.setId(menu.id().orElse(null));
        menuEntity.setParentId(menu.parentId());
        menuEntity.setTitle(menu.title());
        menuEntity.setName(menu.name());
        menuEntity.setUrl(menu.url());
        menuEntity.setComponent(menu.component());
        menuEntity.setIcon(menu.icon());
        menuEntity.setOrderNum(menu.orderNum());
        menuEntity.setType(menu.type());
        menuEntity.setStatus(menu.status());

        menuEntity.setCreated(dealMenu.getCreated());

        return menuEntity;
    }
}
