package wiki.chiu.micro.common.vo;

import java.util.List;

public record MenusAndButtonsRpcVo(

        List<ButtonRpcVo> buttons,

        List<MenuRpcVo> menus) {

    public static MenusAndButtonsRpcVo.MenusAndButtonsRpcVoBuilder builder() {
        return new MenusAndButtonsRpcVo.MenusAndButtonsRpcVoBuilder();
    }

    public static class MenusAndButtonsRpcVoBuilder {
        private List<ButtonRpcVo> buttons;

        private List<MenuRpcVo> menus;


        public MenusAndButtonsRpcVo.MenusAndButtonsRpcVoBuilder buttons(List<ButtonRpcVo> buttons) {
            this.buttons = buttons;
            return this;
        }

        public MenusAndButtonsRpcVo.MenusAndButtonsRpcVoBuilder menus(List<MenuRpcVo> menus) {
            this.menus = menus;
            return this;
        }

        public MenusAndButtonsRpcVo build() {
            return new MenusAndButtonsRpcVo(buttons, menus);
        }
    }
}
