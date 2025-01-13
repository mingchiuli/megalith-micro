package wiki.chiu.micro.common.dto;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record ButtonRpcDto(

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


    public static ButtonRpcDtoBuilder builder() {
        return new ButtonRpcDtoBuilder();
    }

    public static class ButtonRpcDtoBuilder {
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

        public ButtonRpcDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ButtonRpcDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public ButtonRpcDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ButtonRpcDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ButtonRpcDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ButtonRpcDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public ButtonRpcDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public ButtonRpcDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ButtonRpcDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public ButtonRpcDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ButtonRpcDto build() {
            return new ButtonRpcDto(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}

