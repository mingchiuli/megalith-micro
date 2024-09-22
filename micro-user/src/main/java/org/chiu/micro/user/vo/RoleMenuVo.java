package org.chiu.micro.user.vo;

import java.util.ArrayList;
import java.util.List;

public record RoleMenuVo(

        Long menuId,

        String title,

        //是否选了
        Boolean check,

        List<RoleMenuVo> children) {

    public static RoleMenuVoBuilder builder() {
        return new RoleMenuVoBuilder();
    }

    public static class RoleMenuVoBuilder {
        private Long menuId;
        private String title;
        private Boolean check;
        private List<RoleMenuVo> children = new ArrayList<>();


        public RoleMenuVoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public RoleMenuVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public RoleMenuVoBuilder check(Boolean check) {
            this.check = check;
            return this;
        }

        public RoleMenuVoBuilder children(List<RoleMenuVo> children) {
            this.children = children;
            return this;
        }

        public RoleMenuVo build() {
            return new RoleMenuVo(menuId, title, check, children);
        }
    }
}
