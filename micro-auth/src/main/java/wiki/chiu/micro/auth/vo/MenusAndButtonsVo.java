package wiki.chiu.micro.auth.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public record MenusAndButtonsVo(

        MenuWithChildVo menus,

        List<ButtonVo> buttons) {


    public static MenusAndButtonsVoBuilder builder() {
        return new MenusAndButtonsVoBuilder(null, new ArrayList<>());
    }

    public record MenusAndButtonsVoBuilder(
            MenuWithChildVo menus,
            List<ButtonVo> buttons
    ) {

        public MenusAndButtonsVoBuilder menus(MenuWithChildVo menus) {
            return new MenusAndButtonsVoBuilder(menus, buttons);
        }

        public MenusAndButtonsVoBuilder buttons(List<ButtonVo> buttons) {
            return new MenusAndButtonsVoBuilder(menus, buttons);
        }

        public MenusAndButtonsVo build() {
            return new MenusAndButtonsVo(menus, buttons);
        }

    }
}
