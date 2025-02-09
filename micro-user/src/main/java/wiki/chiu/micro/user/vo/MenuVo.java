package wiki.chiu.micro.user.vo;



public record MenuVo(

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

    public static MenuVoBuilder builder() {
        return new MenuVoBuilder();
    }

    public static class MenuVoBuilder {
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

        public MenuVoBuilder id(Long id) {
            this.id = id;
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

        public MenuVo build() {
            return new MenuVo(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}

