package org.chiu.micro.user.vo;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record ButtonVo(

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

    public static ButtonVoBuilder builder() {
        return new ButtonVoBuilder();
    }

    public static class ButtonVoBuilder {
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

        public ButtonVoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public ButtonVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public ButtonVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ButtonVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ButtonVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ButtonVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public ButtonVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public ButtonVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ButtonVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public ButtonVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ButtonVo build() {
            return new ButtonVo(menuId, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}
