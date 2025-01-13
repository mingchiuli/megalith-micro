
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

    public static MenuDto.MenuWithChildRpcDtoBuilder builder() {
        return new MenuDto.MenuWithChildRpcDtoBuilder();
    }

    public static class MenuWithChildRpcDtoBuilder {
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


        public MenuDto.MenuWithChildRpcDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuDto.MenuWithChildRpcDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuDto build() {
            return new MenuDto(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}
