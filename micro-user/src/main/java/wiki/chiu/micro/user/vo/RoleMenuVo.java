package wiki.chiu.micro.user.vo;

import java.util.Collections;
import java.util.List;

public record RoleMenuVo(

        Long menuId,

        String title,

        //是否选了
        Boolean check,

        List<RoleMenuVo> children) {

    public static RoleMenuVoBuilder builder() {
        return new RoleMenuVoBuilder(null, null, null, Collections.emptyList());
    }

    public record RoleMenuVoBuilder(
        Long menuId,
        String title,
        Boolean check,
        List<RoleMenuVo> children) {


        public RoleMenuVoBuilder menuId(Long menuId) {
            return new RoleMenuVoBuilder(menuId, title, check, children);
        }

        public RoleMenuVoBuilder title(String title) {
            return new RoleMenuVoBuilder(menuId, title, check, children);
        }

        public RoleMenuVoBuilder check(Boolean check) {
            return new RoleMenuVoBuilder(menuId, title, check, children);
        }

        public RoleMenuVoBuilder children(List<RoleMenuVo> children) {
            return new RoleMenuVoBuilder(menuId, title, check, children);
        }

        public RoleMenuVo build() {
            return new RoleMenuVo(menuId, title, check, children);
        }
    }
}
