package org.chiu.micro.user.dto;

import java.util.List;


/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record MenusAndButtonsDto(

        List<MenuDto> menus,

        List<ButtonDto> buttons) {


    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder();
    }

    public static class MenusAndButtonsDtoBuilder {
        private List<MenuDto> menus;
        private List<ButtonDto> buttons;

        public MenusAndButtonsDtoBuilder menus(List<MenuDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(menus, buttons);
        }
    }
}
