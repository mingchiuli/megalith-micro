package wiki.chiu.micro.auth.dto;

import java.io.Serializable;

public record MenuDto(
        Long id,
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
        return new MenuDtoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record MenuDtoBuilder(
            Long id,
            Long parentId,
            String title,
            String name,
            String url,
            String component,
            Integer type,
            String icon,
            Integer orderNum,
            Integer status) {

        public MenuDtoBuilder id(Long id) {
            return new MenuDtoBuilder(id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder parentId(Long parentId) {
            return new MenuDtoBuilder(this.id, parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder title(String title) {
            return new MenuDtoBuilder(this.id, this.parentId, title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder name(String name) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder url(String url) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder component(String component) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder type(Integer type) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, type, this.icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder icon(String icon) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, icon, this.orderNum, this.status);
        }

        public MenuDtoBuilder orderNum(Integer orderNum) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, orderNum, this.status);
        }

        public MenuDtoBuilder status(Integer status) {
            return new MenuDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, status);
        }

        public MenuDto build() {
            return new MenuDto(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}