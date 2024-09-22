package org.chiu.micro.auth.dto;

import java.io.Serializable;
import java.util.List;


public class MenusAndButtonsDto implements Serializable {

    private List<ButtonDto> buttons;

    private List<MenuWithChildDto> menus;

    public MenusAndButtonsDto(List<ButtonDto> buttons, List<MenuWithChildDto> menus) {
        this.buttons = buttons;
        this.menus = menus;
    }

    public MenusAndButtonsDto() {
    }

    public static MenusAndButtonsDtoBuilder builder() {
        return new MenusAndButtonsDtoBuilder();
    }

    public List<ButtonDto> getButtons() {
        return this.buttons;
    }

    public List<MenuWithChildDto> getMenus() {
        return this.menus;
    }

    public void setButtons(List<ButtonDto> buttons) {
        this.buttons = buttons;
    }

    public void setMenus(List<MenuWithChildDto> menus) {
        this.menus = menus;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MenusAndButtonsDto)) return false;
        final MenusAndButtonsDto other = (MenusAndButtonsDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$buttons = this.getButtons();
        final Object other$buttons = other.getButtons();
        if (this$buttons == null ? other$buttons != null : !this$buttons.equals(other$buttons)) return false;
        final Object this$menus = this.getMenus();
        final Object other$menus = other.getMenus();
        if (this$menus == null ? other$menus != null : !this$menus.equals(other$menus)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MenusAndButtonsDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $buttons = this.getButtons();
        result = result * PRIME + ($buttons == null ? 43 : $buttons.hashCode());
        final Object $menus = this.getMenus();
        result = result * PRIME + ($menus == null ? 43 : $menus.hashCode());
        return result;
    }

    public String toString() {
        return "MenusAndButtonsDto(buttons=" + this.getButtons() + ", menus=" + this.getMenus() + ")";
    }

    public static class MenusAndButtonsDtoBuilder {
        private List<ButtonDto> buttons;
        private List<MenuWithChildDto> menus;

        MenusAndButtonsDtoBuilder() {
        }

        public MenusAndButtonsDtoBuilder buttons(List<ButtonDto> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsDtoBuilder menus(List<MenuWithChildDto> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsDto build() {
            return new MenusAndButtonsDto(this.buttons, this.menus);
        }

        public String toString() {
            return "MenusAndButtonsDto.MenusAndButtonsDtoBuilder(buttons=" + this.buttons + ", menus=" + this.menus + ")";
        }
    }
}
