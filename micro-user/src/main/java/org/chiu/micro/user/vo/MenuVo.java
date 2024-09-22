package org.chiu.micro.user.vo;


import java.util.ArrayList;
import java.util.List;


public record MenuVo(

        Long menuId,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        Integer type,

        String icon,

        Integer orderNum,

        Integer status,

        List<MenuVo> children) {

    public static MenuVoBuilder builder() {
        return new MenuVoBuilder();
    }

    public static class MenuVoBuilder {
        private Long menuId;
        private Long parentId;
        private String title;
        private String name;
        private String url;
        private String component;
        private Integer type;
        private String icon;
        private Integer orderNum;
        private Integer status;
        private List<MenuVo> children = new ArrayList<>();

        public MenuVoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuVoBuilder children(List<MenuVo> children) {
            this.children = children;
            return this;
        }

        public MenuVo build() {
            return new MenuVo(menuId, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }
    }
}

