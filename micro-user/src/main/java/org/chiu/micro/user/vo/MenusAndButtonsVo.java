package org.chiu.micro.user.vo;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public class MenusAndButtonsVo {

    private List<MenuWithChildVo> menus;

    private List<ButtonVo> buttons;

    MenusAndButtonsVo(List<MenuWithChildVo> menus, List<ButtonVo> buttons) {
        this.menus = menus;
        this.buttons = buttons;
    }

    public static MenusAndButtonsVoBuilder builder() {
        return new MenusAndButtonsVoBuilder();
    }

    public List<MenuWithChildVo> getMenus() {
        return this.menus;
    }

    public List<ButtonVo> getButtons() {
        return this.buttons;
    }

    public void setMenus(List<MenuWithChildVo> menus) {
        this.menus = menus;
    }

    public void setButtons(List<ButtonVo> buttons) {
        this.buttons = buttons;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MenusAndButtonsVo)) return false;
        final MenusAndButtonsVo other = (MenusAndButtonsVo) o;
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
        return other instanceof MenusAndButtonsVo;
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
        return "MenusAndButtonsVo(menus=" + this.getMenus() + ", buttons=" + this.getButtons() + ")";
    }

    public static class MenusAndButtonsVoBuilder {
        private List<MenuWithChildVo> menus;
        private List<ButtonVo> buttons;

        MenusAndButtonsVoBuilder() {
        }

        public MenusAndButtonsVoBuilder menus(List<MenuWithChildVo> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsVoBuilder buttons(List<ButtonVo> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsVo build() {
            return new MenusAndButtonsVo(this.menus, this.buttons);
        }

        public String toString() {
            return "MenusAndButtonsVo.MenusAndButtonsVoBuilder(menus=" + this.menus + ", buttons=" + this.buttons + ")";
        }
    }
}
