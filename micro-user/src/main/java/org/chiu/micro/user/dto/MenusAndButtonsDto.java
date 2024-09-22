package org.chiu.micro.user.dto;

import java.util.List;


/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public class MenusAndButtonsDto {

    private List<MenuDto> menus;

    private List<ButtonDto> buttons;

    MenusAndButtonsDto(List<MenuDto> menus, List<ButtonDto> buttons) {
        this.menus = menus;
        this.buttons = buttons;
    }

    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder();
    }

    public List<MenuDto> getMenus() {
        return this.menus;
    }

    public List<ButtonDto> getButtons() {
        return this.buttons;
    }

    public void setMenus(List<MenuDto> menus) {
        this.menus = menus;
    }

    public void setButtons(List<ButtonDto> buttons) {
        this.buttons = buttons;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MenusAndButtonsDto)) return false;
        final MenusAndButtonsDto other = (MenusAndButtonsDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$menus = this.getMenus();
        final Object other$menus = other.getMenus();
        if (this$menus == null ? other$menus != null : !this$menus.equals(other$menus)) return false;
        final Object this$buttons = this.getButtons();
        final Object other$buttons = other.getButtons();
        if (this$buttons == null ? other$buttons != null : !this$buttons.equals(other$buttons)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MenusAndButtonsDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $menus = this.getMenus();
        result = result * PRIME + ($menus == null ? 43 : $menus.hashCode());
        final Object $buttons = this.getButtons();
        result = result * PRIME + ($buttons == null ? 43 : $buttons.hashCode());
        return result;
    }

    public String toString() {
        return "MenusAndButtonsDto(menus=" + this.getMenus() + ", buttons=" + this.getButtons() + ")";
    }

    public static class MenusAndButtonsDtoBuilder {
        private List<MenuDto> menus;
        private List<ButtonDto> buttons;

        MenusAndButtonsDtoBuilder() {
        }

        public MenusAndButtonsDtoBuilder menus(List<MenuDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(this.menus, this.buttons);
        }

        public String toString() {
            return "MenusAndButtonsDto.MenusAndButtonsDtoBuilder(menus=" + this.menus + ", buttons=" + this.buttons + ")";
        }
    }
}
