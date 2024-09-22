package org.chiu.micro.user.vo;

import java.util.ArrayList;
import java.util.List;

public class RoleMenuVo {

    private Long menuId;

    private String title;

    //是否选了
    private Boolean check;

    private List<RoleMenuVo> children = new ArrayList<>();

    RoleMenuVo(Long menuId, String title, Boolean check, List<RoleMenuVo> children) {
        this.menuId = menuId;
        this.title = title;
        this.check = check;
        this.children = children;
    }

    private static List<RoleMenuVo> $default$children() {
        return new ArrayList<>();
    }

    public static RoleMenuVoBuilder builder() {
        return new RoleMenuVoBuilder();
    }

    public Long getMenuId() {
        return this.menuId;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getCheck() {
        return this.check;
    }

    public List<RoleMenuVo> getChildren() {
        return this.children;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public void setChildren(List<RoleMenuVo> children) {
        this.children = children;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleMenuVo)) return false;
        final RoleMenuVo other = (RoleMenuVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$menuId = this.getMenuId();
        final Object other$menuId = other.getMenuId();
        if (this$menuId == null ? other$menuId != null : !this$menuId.equals(other$menuId)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$check = this.getCheck();
        final Object other$check = other.getCheck();
        if (this$check == null ? other$check != null : !this$check.equals(other$check)) return false;
        final Object this$children = this.getChildren();
        final Object other$children = other.getChildren();
        if (this$children == null ? other$children != null : !this$children.equals(other$children)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RoleMenuVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $menuId = this.getMenuId();
        result = result * PRIME + ($menuId == null ? 43 : $menuId.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $check = this.getCheck();
        result = result * PRIME + ($check == null ? 43 : $check.hashCode());
        final Object $children = this.getChildren();
        result = result * PRIME + ($children == null ? 43 : $children.hashCode());
        return result;
    }

    public String toString() {
        return "RoleMenuVo(menuId=" + this.getMenuId() + ", title=" + this.getTitle() + ", check=" + this.getCheck() + ", children=" + this.getChildren() + ")";
    }

    public static class RoleMenuVoBuilder {
        private Long menuId;
        private String title;
        private Boolean check;
        private List<RoleMenuVo> children$value;
        private boolean children$set;

        RoleMenuVoBuilder() {
        }

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
            this.children$value = children;
            this.children$set = true;
            return this;
        }

        public RoleMenuVo build() {
            List<RoleMenuVo> children$value = this.children$value;
            if (!this.children$set) {
                children$value = RoleMenuVo.$default$children();
            }
            return new RoleMenuVo(this.menuId, this.title, this.check, children$value);
        }

        public String toString() {
            return "RoleMenuVo.RoleMenuVoBuilder(menuId=" + this.menuId + ", title=" + this.title + ", check=" + this.check + ", children$value=" + this.children$value + ")";
        }
    }
}
