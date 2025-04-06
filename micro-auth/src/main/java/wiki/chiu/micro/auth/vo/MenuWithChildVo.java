package wiki.chiu.micro.auth.vo;

import java.util.ArrayList;
import java.util.List;

public record MenuWithChildVo(
        Long id,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        Integer type,

        String icon,

        Integer orderNum,

        Integer status,

        List<MenuWithChildVo> children) {

    public static MenuWithChildVoBuilder builder() {
        return new MenuWithChildVoBuilder();
    }

    public static class MenuWithChildVoBuilder {
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
        private List<MenuWithChildVo> children = new ArrayList<>();

        public MenuWithChildVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuWithChildVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuWithChildVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuWithChildVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuWithChildVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuWithChildVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuWithChildVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuWithChildVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuWithChildVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuWithChildVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuWithChildVoBuilder children(List<MenuWithChildVo> children) {
            this.children = children;
            return this;
        }

        public MenuWithChildVo build() {
            return new MenuWithChildVo(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }
    }
}
