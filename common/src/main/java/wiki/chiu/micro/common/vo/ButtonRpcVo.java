package wiki.chiu.micro.common.vo;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record ButtonRpcVo(
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


    public static ButtonRpcVoBuilder builder() {
        return new ButtonRpcVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record ButtonRpcVoBuilder(
            Long id,
            Long parentId,
            String title,
            String name,
            String url,
            String component,
            Integer type,
            String icon,
            Integer orderNum,
            Integer status
    ) {

        public ButtonRpcVoBuilder id(Long id) {
            return new ButtonRpcVoBuilder(id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder parentId(Long parentId) {
            return new ButtonRpcVoBuilder(this.id, parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder title(String title) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder name(String name) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder url(String url) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder component(String component) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder type(Integer type) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, type, this.icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder icon(String icon) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, icon, this.orderNum, this.status);
        }

        public ButtonRpcVoBuilder orderNum(Integer orderNum) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, orderNum, this.status);
        }

        public ButtonRpcVoBuilder status(Integer status) {
            return new ButtonRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, status);
        }

        public ButtonRpcVo build() {
            return new ButtonRpcVo(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}