package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record MenuDisplayVo(

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

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        List<MenuDisplayVo> children) {

    public MenuDisplayVo(MenuDisplayVo vo, Long parentId, List<MenuDisplayVo> children) {
        this(vo.id, parentId, vo.title, vo.name, vo.url, vo.component, vo.type, vo.icon, vo.orderNum, vo.status, vo.created, vo.updated, children);
    }

    public static MenuDisplayVoBuilder builder() {
        return new MenuDisplayVoBuilder(null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList());
    }

    public record MenuDisplayVoBuilder(
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
            LocalDateTime created,
            LocalDateTime updated,
            List<MenuDisplayVo> children
    ) {
        public MenuDisplayVoBuilder id(Long id) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder parentId(Long parentId) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder title(String title) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder name(String name) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder url(String url) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder component(String component) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder type(Integer type) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder icon(String icon) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder orderNum(Integer orderNum) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder status(Integer status) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder created(LocalDateTime created) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder updated(LocalDateTime updated) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVoBuilder children(List<MenuDisplayVo> children) {
            return new MenuDisplayVoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayVo build() {
            return new MenuDisplayVo(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }
    }
}
