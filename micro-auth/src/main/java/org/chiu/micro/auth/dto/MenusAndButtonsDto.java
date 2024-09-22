package org.chiu.micro.auth.dto;

import java.io.Serializable;
import java.util.List;


public record MenusAndButtonsDto(

        List<ButtonDto> buttons,

        List<MenuWithChildDto> menus) implements Serializable {


    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder();
    }

    public static class MenusAndButtonsDtoBuilder {

        private List<ButtonDto> buttons;
        private List<MenuWithChildDto> menus;

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsDtoBuilder menus(List<MenuWithChildDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(buttons, menus);
        }

    }
}
