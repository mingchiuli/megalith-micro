package wiki.chiu.micro.auth.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public record MenusAndButtonsDto(
        List<ButtonDto> buttons,
        List<MenuDto> menus) implements Serializable {

    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder(new ArrayList<>(), new ArrayList<>());
    }

    public record MenusAndButtonsDtoBuilder(
            List<ButtonDto> buttons,
            List<MenuDto> menus
    ) {

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            return new MenusAndButtonsDtoBuilder(buttons, menus);
        }

        public MenusAndButtonsDtoBuilder menus(List<MenuDto> menus) {
            return new MenusAndButtonsDtoBuilder(buttons, menus);

        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(buttons, menus);
        }
    }
}