package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return new MenuDisplayVoBuilder();
    }

    public static class MenuDisplayVoBuilder {
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
        private LocalDateTime created;
        private LocalDateTime updated;
        private List<MenuDisplayVo> children = new ArrayList<>();

        public MenuDisplayVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuDisplayVoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuDisplayVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuDisplayVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuDisplayVoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuDisplayVoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuDisplayVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuDisplayVoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuDisplayVoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuDisplayVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public MenuDisplayVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public MenuDisplayVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public MenuDisplayVoBuilder children(List<MenuDisplayVo> children) {
            this.children = children;
            return this;
        }

        public MenuDisplayVo build() {
            return new MenuDisplayVo(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }
    }
}
