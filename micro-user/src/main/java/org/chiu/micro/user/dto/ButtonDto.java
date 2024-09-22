package org.chiu.micro.user.dto;

import java.io.Serializable;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:17
 **/
public class ButtonDto implements Serializable {

    private Long menuId;

    private Long parentId;

    private String title;

    private String name;

    private String url;

    private String component;

    private Integer type;

    private String icon;

    private Integer orderNum;

    private Integer status;

    ButtonDto(Long menuId, Long parentId, String title, String name, String url, String component, Integer type, String icon, Integer orderNum, Integer status) {
        this.menuId = menuId;
        this.parentId = parentId;
        this.title = title;
        this.name = name;
        this.url = url;
        this.component = component;
        this.type = type;
        this.icon = icon;
        this.orderNum = orderNum;
        this.status = status;
    }

    public static ButtonDtoBuilder builder() {
        return new ButtonDtoBuilder();
    }

    public Long getMenuId() {
        return this.menuId;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getComponent() {
        return this.component;
    }

    public Integer getType() {
        return this.type;
    }

    public String getIcon() {
        return this.icon;
    }

    public Integer getOrderNum() {
        return this.orderNum;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ButtonDto)) return false;
        final ButtonDto other = (ButtonDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$menuId = this.getMenuId();
        final Object other$menuId = other.getMenuId();
        if (this$menuId == null ? other$menuId != null : !this$menuId.equals(other$menuId)) return false;
        final Object this$parentId = this.getParentId();
        final Object other$parentId = other.getParentId();
        if (this$parentId == null ? other$parentId != null : !this$parentId.equals(other$parentId)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        final Object this$component = this.getComponent();
        final Object other$component = other.getComponent();
        if (this$component == null ? other$component != null : !this$component.equals(other$component)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$icon = this.getIcon();
        final Object other$icon = other.getIcon();
        if (this$icon == null ? other$icon != null : !this$icon.equals(other$icon)) return false;
        final Object this$orderNum = this.getOrderNum();
        final Object other$orderNum = other.getOrderNum();
        if (this$orderNum == null ? other$orderNum != null : !this$orderNum.equals(other$orderNum)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ButtonDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $menuId = this.getMenuId();
        result = result * PRIME + ($menuId == null ? 43 : $menuId.hashCode());
        final Object $parentId = this.getParentId();
        result = result * PRIME + ($parentId == null ? 43 : $parentId.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $component = this.getComponent();
        result = result * PRIME + ($component == null ? 43 : $component.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $icon = this.getIcon();
        result = result * PRIME + ($icon == null ? 43 : $icon.hashCode());
        final Object $orderNum = this.getOrderNum();
        result = result * PRIME + ($orderNum == null ? 43 : $orderNum.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "ButtonDto(menuId=" + this.getMenuId() + ", parentId=" + this.getParentId() + ", title=" + this.getTitle() + ", name=" + this.getName() + ", url=" + this.getUrl() + ", component=" + this.getComponent() + ", type=" + this.getType() + ", icon=" + this.getIcon() + ", orderNum=" + this.getOrderNum() + ", status=" + this.getStatus() + ")";
    }

    public static class ButtonDtoBuilder {
        private Long menuId;
        private Long parentId;
        private String title;
        private String name;
        private String url;
        private String component;
        private Integer type;
        private String icon;
        private Integer orderNum;
        private Integer status;

        ButtonDtoBuilder() {
        }

        public ButtonDtoBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public ButtonDtoBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public ButtonDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ButtonDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ButtonDtoBuilder url(String url) {
            this.url = url;
            return this;
        }

        public ButtonDtoBuilder component(String component) {
            this.component = component;
            return this;
        }

        public ButtonDtoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public ButtonDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public ButtonDtoBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public ButtonDtoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public ButtonDto build() {
            return new ButtonDto(this.menuId, this.parentId, this.title, this.name, this.url, this.component, this.type, this.icon, this.orderNum, this.status);
        }

        public String toString() {
            return "ButtonDto.ButtonDtoBuilder(menuId=" + this.menuId + ", parentId=" + this.parentId + ", title=" + this.title + ", name=" + this.name + ", url=" + this.url + ", component=" + this.component + ", type=" + this.type + ", icon=" + this.icon + ", orderNum=" + this.orderNum + ", status=" + this.status + ")";
        }
    }
}
