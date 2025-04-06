package wiki.chiu.micro.auth.dto;

import java.util.ArrayList;
import java.util.List;

public record MenuWithChildDto(
        Long id,
        Long parentId,
        String title,
        String name,
        String url,
        String component,
        Integer type,
        String icon,
        Integer orderNum,
        Integer status,
        List<MenuWithChildDto> children) {

    public static MenuWithChildDtoBuilder builder() {
        return new MenuWithChildDtoBuilder(null, null, null, null, null, null, null, null, null, null, new ArrayList<>());
    }

    public record MenuWithChildDtoBuilder(
            Long id,
            Long parentId,
            String title,
            String name,
            String url,
            String component,
            Integer type,
            String icon,
            Integer orderNum,
            Integer status,
            List<MenuWithChildDto> children) {

        public MenuWithChildDtoBuilder id(Long id) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder parentId(Long parentId) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder title(String title) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder name(String name) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder url(String url) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder component(String component) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder type(Integer type) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder icon(String icon) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder orderNum(Integer orderNum) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder status(Integer status) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDtoBuilder children(List<MenuWithChildDto> children) {
            return new MenuWithChildDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildDto build() {
            return new MenuWithChildDto(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }
    }
}