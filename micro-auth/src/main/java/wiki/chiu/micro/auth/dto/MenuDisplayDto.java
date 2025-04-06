package wiki.chiu.micro.auth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record MenuDisplayDto(
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
        List<MenuDisplayDto> children) {

    public MenuDisplayDto(MenuDisplayDto dto, Long parentId, List<MenuDisplayDto> children) {
        this(dto.id, parentId, dto.title, dto.name, dto.url, dto.component, dto.type, dto.icon, dto.orderNum, dto.status, dto.created, dto.updated, children);
    }

    public static MenuDisplayDtoBuilder builder() {
        return new MenuDisplayDtoBuilder(null, null, null, null, null, null, null, null, null, null, null, null, new ArrayList<>());
    }

    public record MenuDisplayDtoBuilder(
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
            List<MenuDisplayDto> children) {

        public MenuDisplayDtoBuilder() {
            this(null, null, null, null, null, null, null, null, null, null, null, null, new ArrayList<>());
        }

        public MenuDisplayDtoBuilder id(Long id) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder parentId(Long parentId) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder title(String title) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder name(String name) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder url(String url) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder component(String component) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder type(Integer type) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder icon(String icon) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder orderNum(Integer orderNum) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder status(Integer status) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder created(LocalDateTime created) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder updated(LocalDateTime updated) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDtoBuilder children(List<MenuDisplayDto> children) {
            return new MenuDisplayDtoBuilder(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }

        public MenuDisplayDto build() {
            return new MenuDisplayDto(id, parentId, title, name, url, component, type, icon, orderNum, status, created, updated, children);
        }
    }
}