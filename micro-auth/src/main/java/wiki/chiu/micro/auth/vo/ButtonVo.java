package wiki.chiu.micro.auth.vo;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record ButtonVo(
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

    public static ButtonVoBuilder builder() {
        return new ButtonVoBuilder(null, null,null,null,null,null,null,null, null,null);
    }

    public record ButtonVoBuilder(
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


        public ButtonVoBuilder id(Long id) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder parentId(Long parentId) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder title(String title) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder name(String name) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder url(String url) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder component(String component) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder type(Integer type) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder icon(String icon) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder orderNum(Integer orderNum) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVoBuilder status(Integer status) {
            return new ButtonVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }

        public ButtonVo build() {
            return new ButtonVo(id, parentId, title, name, url, component, type, icon, orderNum, status);
        }
    }
}