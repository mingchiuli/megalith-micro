package wiki.chiu.micro.user.vo;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record MenusAndButtonsRpcVo(

        List<MenuVo> menus,

        List<ButtonVo> buttons) {

    public static MenusAndButtonsVoBuilder builder() {
        return new MenusAndButtonsVoBuilder();
    }

    public static class MenusAndButtonsVoBuilder {
        private List<MenuVo> menus;
        private List<ButtonVo> buttons;

        public MenusAndButtonsVoBuilder menus(List<MenuVo> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsVoBuilder buttons(List<ButtonVo> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsRpcVo build() {
            return new MenusAndButtonsRpcVo(menus, buttons);
        }
    }
}
