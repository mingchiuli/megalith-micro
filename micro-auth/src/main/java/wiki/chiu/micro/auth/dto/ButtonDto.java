package wiki.chiu.micro.auth.dto;


import java.io.Serializable;

public record ButtonDto(
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
) implements Serializable {

    public static ButtonDto.ButtonDtoBuilder builder() {
        return new ButtonDto.ButtonDtoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record ButtonDtoBuilder(
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

        public ButtonDtoBuilder id(Long id) {
            return new ButtonDtoBuilder(id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder parentId(Long parentId) {
            return new ButtonDtoBuilder(this.id, parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder title(String title) {
            return new ButtonDtoBuilder(this.id, this.parentId, title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder name(String name) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder url(String url) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder component(String component) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, component, this.type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder type(Integer type) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, type, this.icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder icon(String icon) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, icon, this.orderNum, this.status);
        }

        public ButtonDtoBuilder orderNum(Integer orderNum) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, orderNum, this.status);
        }

        public ButtonDtoBuilder status(Integer status) {
            return new ButtonDtoBuilder(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, status);
        }

        public ButtonDto build() {
            return new ButtonDto(this.id, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}