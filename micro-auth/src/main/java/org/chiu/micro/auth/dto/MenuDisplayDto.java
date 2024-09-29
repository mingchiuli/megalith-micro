package org.chiu.micro.auth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record MenuDisplayDto(

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

        LocalDateTime created,

        LocalDateTime updated,

        List<MenuDisplayDto> children) {

    public static MenuDisplayDtoBuilder builder() {
        return new MenuDisplayDtoBuilder();
    }

    public static class MenuDisplayDtoBuilder {
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
        private LocalDateTime created;
        private LocalDateTime updated;
        private List<MenuDisplayDto> children = new ArrayList<>();

        public MenuDisplayDtoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuDisplayDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuDisplayDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuDisplayDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuDisplayDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuDisplayDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuDisplayDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuDisplayDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuDisplayDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuDisplayDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuDisplayDtoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public MenuDisplayDtoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public MenuDisplayDtoBuilder children(List<MenuDisplayDto> children) {
            this.children = children;
            return this;
        }

        public MenuDisplayDto build() {
            return new MenuDisplayDto(menuId, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }
    }
}
