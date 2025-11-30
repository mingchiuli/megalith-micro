package wiki.chiu.micro.auth.dto;

import java.util.List;


public record MenusAndButtonsDto(

        List<ButtonDto> buttons,

        List<MenuDto> menus) {


    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder();
    }

    public static class MenusAndButtonsDtoBuilder {

        private List<ButtonDto> buttons;
        private List<MenuDto> menus;

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsDtoBuilder menus(List<MenuDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(buttons, menus);
        }

    }
}
