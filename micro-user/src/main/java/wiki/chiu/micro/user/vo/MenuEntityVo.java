package wiki.chiu.micro.user.vo;

public record MenuEntityVo(
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
    public static MenuEntityVoBuilder builder() {
        return new MenuEntityVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record MenuEntityVoBuilder(
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
        public MenuEntityVoBuilder id(Long id) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder parentId(Long parentId) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder title(String title) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder name(String name) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder url(String url) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder component(String component) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder type(Integer type) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder icon(String icon) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder orderNum(Integer orderNum) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVoBuilder status(Integer status) {
            return new MenuEntityVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public MenuEntityVo build() {
            return new MenuEntityVo(
                id,
                parentId,
                title,
                name,
                url,
                component,
                type,
                icon,
                orderNum,
                status
            );
        }
    }
}
