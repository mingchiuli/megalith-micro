package org.chiu.micro.auth.dto;

import java.util.List;

public class MenusAndButtonsRpcDto {

    private List<ButtonRpcDto> buttons;

    private List<MenuWithChildRpcDto> menus;

    public MenusAndButtonsRpcDto() {
    }

    public List<ButtonRpcDto> getButtons() {
        return this.buttons;
    }

    public List<MenuWithChildRpcDto> getMenus() {
        return this.menus;
    }

    public void setButtons(List<ButtonRpcDto> buttons) {
        this.buttons = buttons;
    }

    public void setMenus(List<MenuWithChildRpcDto> menus) {
        this.menus = menus;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MenusAndButtonsRpcDto)) return false;
        final MenusAndButtonsRpcDto other = (MenusAndButtonsRpcDto) o;
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
        return other instanceof MenusAndButtonsRpcDto;
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
        return "MenusAndButtonsRpcDto(buttons=" + this.getButtons() + ", menus=" + this.getMenus() + ")";
    }
}
