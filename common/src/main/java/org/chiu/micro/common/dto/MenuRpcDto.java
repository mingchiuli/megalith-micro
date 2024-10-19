package org.chiu.micro.common.dto;


public record MenuRpcDto(
        Long menuId,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        Integer type,

        String icon,

        Integer orderNum,

        Integer status) {

    public static class MenuWithChildRpcDtoBuilder {
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


        public MenuWithChildRpcDtoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuWithChildRpcDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuWithChildRpcDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuWithChildRpcDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuWithChildRpcDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuWithChildRpcDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuWithChildRpcDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuWithChildRpcDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuWithChildRpcDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuWithChildRpcDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuRpcDto build() {
            return new MenuRpcDto(this.menuId, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}
