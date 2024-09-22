package org.chiu.micro.user.vo;

public record MenuEntityVo(

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

    public static MenuEntityVoBuilder builder() {
        return new MenuEntityVoBuilder();
    }

    public static class MenuEntityVoBuilder {
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

        public MenuEntityVoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuEntityVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuEntityVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuEntityVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuEntityVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuEntityVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuEntityVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuEntityVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuEntityVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuEntityVo build() {
            return new MenuEntityVo(menuId, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}
