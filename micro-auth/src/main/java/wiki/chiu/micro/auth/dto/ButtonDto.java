package wiki.chiu.micro.auth.dto;

import java.io.Serializable;


/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record ButtonDto (

        Long menuId,

        Long parentId,

        String title,

        String name,

        String url,

        String component,

        Integer type,

        String icon,

        Integer orderNum,

        Integer status) implements Serializable {

    public static ButtonDtoBuilder builder() {
        return new ButtonDtoBuilder();
    }


    public static class ButtonDtoBuilder {

        private Long menuId;
        private Long parentId;
        private String title;
        private String name;
        private String url;
        private String component;
        private Integer type;
        private String icon;
        private Integer orderNum;
        private Integer status;

        public ButtonDtoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public ButtonDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public ButtonDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ButtonDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ButtonDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ButtonDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public ButtonDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public ButtonDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ButtonDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public ButtonDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ButtonDto build() {
            return new ButtonDto(this.menuId, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }
    }
}

