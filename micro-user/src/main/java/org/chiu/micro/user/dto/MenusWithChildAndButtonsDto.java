package org.chiu.micro.user.dto;

import java.util.List;

public record MenusWithChildAndButtonsDto(

        List<MenuWithChildDto> menus,

        List<ButtonDto> buttons) {

    public static MenusWithChildAndButtonsDtoBuilder builder() {
        return new MenusWithChildAndButtonsDtoBuilder();
    }

    public static class MenusWithChildAndButtonsDtoBuilder {
        private List<MenuWithChildDto> menus;
        private List<ButtonDto> buttons;


        public MenusWithChildAndButtonsDtoBuilder menus(List<MenuWithChildDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusWithChildAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusWithChildAndButtonsDto build() {
            return new MenusWithChildAndButtonsDto(menus, buttons);
        }
    }
}
