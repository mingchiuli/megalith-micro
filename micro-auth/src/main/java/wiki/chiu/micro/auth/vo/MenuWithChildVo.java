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
        return new MenuWithChildVoBuilder(null, null, null, null, null, null, null, null, null, null, new ArrayList<>());
    }

    public record MenuWithChildVoBuilder(
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
             List<MenuWithChildVo> children
    ) {


        public MenuWithChildVoBuilder id(Long id) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder parentId(Long parentId) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder title(String title) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder name(String name) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder url(String url) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder component(String component) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder type(Integer type) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder icon(String icon) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder orderNum(Integer orderNum) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder status(Integer status) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVoBuilder children(List<MenuWithChildVo> children) {
            return new MenuWithChildVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }

        public MenuWithChildVo build() {
            return new MenuWithChildVo(id, parentId, title, name, url, component, type, icon, orderNum, status, children);
        }
    }
}