package org.chiu.micro.user.dto;

import java.io.Serializable;

public record MenuDto(


        Long menuId,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        Integer type,

        String icon,

        Integer orderNum,

        Integer status) implements Serializable {

    public static MenuDtoBuilder builder() {
        return new MenuDtoBuilder();
    }

    public static class MenuDtoBuilder {
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


        public MenuDtoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuDto build() {
            return new MenuDto(menuId, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}
