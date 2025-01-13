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
        return new MenuWithChildDtoBuilder();
    }

    public static class MenuWithChildDtoBuilder {
        private Long id;
        private Long parentId;
        private String title;
        private String name;
        private String url;
        private String component;
        private Integer type;
        private String icon;
        private Integer orderNum;
        private Integer status;
        private List<MenuWithChildDto> children = new ArrayList<>();

        public MenuWithChildDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuWithChildDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuWithChildDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuWithChildDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuWithChildDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuWithChildDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuWithChildDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuWithChildDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuWithChildDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuWithChildDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuWithChildDtoBuilder children(List<MenuWithChildDto> children) {
            this.children = children;
            return this;
        }

        public MenuWithChildDto build() {
            return new MenuWithChildDto(id, parentId, title,name, url, component, type, icon, orderNum, status, children);
        }

    }
}
