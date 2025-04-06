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


    public static MenuRpcVoBuilder builder() {
        return new MenuRpcVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record MenuRpcVoBuilder(
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

        public MenuRpcVoBuilder id(Long id) {
            return new MenuRpcVoBuilder(id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder parentId(Long parentId) {
            return new MenuRpcVoBuilder(this.id, parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder title(String title) {
            return new MenuRpcVoBuilder(this.id, this.parentId, title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder name(String name) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder url(String url) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder component(String component) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, component, this.type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder type(Integer type) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, type, this.icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder icon(String icon) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, icon, this.orderNum, this.status);
        }

        public MenuRpcVoBuilder orderNum(Integer orderNum) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, orderNum, this.status);
        }

        public MenuRpcVoBuilder status(Integer status) {
            return new MenuRpcVoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, status);
        }

        public MenuRpcVo build() {
            return new MenuRpcVo(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}