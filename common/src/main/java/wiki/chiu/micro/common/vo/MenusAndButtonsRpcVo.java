package wiki.chiu.micro.common.vo;

import java.util.Collections;
import java.util.List;

public record MenusAndButtonsRpcVo(
        List<ButtonRpcVo> buttons,
        List<MenuRpcVo> menus) {

    public static MenusAndButtonsRpcVo builder() {
        return new MenusAndButtonsRpcVo(Collections.emptyList(), Collections.emptyList());
    }

    public record MenusAndButtonsRpcVoBuilder(
            List<ButtonRpcVo> buttons,
            List<MenuRpcVo> menus
    ) {

        public MenusAndButtonsRpcVoBuilder buttons(List<ButtonRpcVo> buttons) {
            return new MenusAndButtonsRpcVoBuilder(buttons, this.menus);
        }

        public MenusAndButtonsRpcVoBuilder menus(List<MenuRpcVo> menus) {
            return new MenusAndButtonsRpcVoBuilder(this.buttons, menus);
        }

        public MenusAndButtonsRpcVo build() {
            return new MenusAndButtonsRpcVo(buttons, menus);
        }
    }
}