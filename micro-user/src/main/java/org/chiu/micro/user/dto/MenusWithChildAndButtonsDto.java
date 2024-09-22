package org.chiu.micro.user.dto;

import java.util.List;

public class MenusWithChildAndButtonsDto {

    private List<MenuWithChildDto> menus;

    private List<ButtonDto> buttons;

    MenusWithChildAndButtonsDto(List<MenuWithChildDto> menus, List<ButtonDto> buttons) {
        this.menus = menus;
        this.buttons = buttons;
    }

    public static MenusWithChildAndButtonsDtoBuilder builder() {
        return new MenusWithChildAndButtonsDtoBuilder();
    }

    public List<MenuWithChildDto> getMenus() {
        return this.menus;
    }

    public List<ButtonDto> getButtons() {
        return this.buttons;
    }

    public void setMenus(List<MenuWithChildDto> menus) {
        this.menus = menus;
    }

    public void setButtons(List<ButtonDto> buttons) {
        this.buttons = buttons;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MenusWithChildAndButtonsDto)) return false;
        final MenusWithChildAndButtonsDto other = (MenusWithChildAndButtonsDto) o;
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
        return other instanceof MenusWithChildAndButtonsDto;
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
        return "MenusWithChildAndButtonsDto(menus=" + this.getMenus() + ", buttons=" + this.getButtons() + ")";
    }

    public static class MenusWithChildAndButtonsDtoBuilder {
        private List<MenuWithChildDto> menus;
        private List<ButtonDto> buttons;

        MenusWithChildAndButtonsDtoBuilder() {
        }

        public MenusWithChildAndButtonsDtoBuilder menus(List<MenuWithChildDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusWithChildAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusWithChildAndButtonsDto build() {
            return new MenusWithChildAndButtonsDto(this.menus, this.buttons);
        }

        public String toString() {
            return "MenusWithChildAndButtonsDto.MenusWithChildAndButtonsDtoBuilder(menus=" + this.menus + ", buttons=" + this.buttons + ")";
        }
    }
}
