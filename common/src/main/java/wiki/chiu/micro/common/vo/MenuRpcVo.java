package wiki.chiu.micro.common.vo;


public record MenuRpcVo(
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

    public static MenuRpcVo.MenuRpcVoBuilder builder() {
        return new MenuRpcVo.MenuRpcVoBuilder();
    }


    public static class MenuRpcVoBuilder {
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


        public MenuRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuRpcVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuRpcVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuRpcVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuRpcVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuRpcVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuRpcVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuRpcVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuRpcVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuRpcVo build() {
            return new MenuRpcVo(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}
