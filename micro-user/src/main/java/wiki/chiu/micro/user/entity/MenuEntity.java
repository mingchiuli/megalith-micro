package wiki.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wiki.chiu.micro.common.lang.Const;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:39 am
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = Const.MENU_TABLE, uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class MenuEntity {

    @Id
    @Column(name = "menu_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "title")
    private String title;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "component")
    private String component;

    @Column(name = "type")
    private Integer type;

    @Column(name = "icon")
    private String icon;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    public MenuEntity(Long menuId, Long parentId, String title, String name, String url, String component, Integer type, String icon, Integer orderNum, Integer status, LocalDateTime created, LocalDateTime updated) {
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
        this.created = created;
        this.updated = updated;
    }

    public MenuEntity() {
    }

    public static MenuEntityBuilder builder() {
        return new MenuEntityBuilder();
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

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
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

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuEntity that)) return false;

        return Objects.equals(menuId, that.menuId) && Objects.equals(parentId, that.parentId) && Objects.equals(title, that.title) && Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(component, that.component) && Objects.equals(type, that.type) && Objects.equals(icon, that.icon) && Objects.equals(orderNum, that.orderNum) && Objects.equals(status, that.status) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(menuId);
        result = 31 * result + Objects.hashCode(parentId);
        result = 31 * result + Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(url);
        result = 31 * result + Objects.hashCode(component);
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(icon);
        result = 31 * result + Objects.hashCode(orderNum);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
    }

    public static class MenuEntityBuilder {
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
        private LocalDateTime created;
        private LocalDateTime updated;

        MenuEntityBuilder() {
        }

        public MenuEntityBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public MenuEntityBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public MenuEntityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MenuEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MenuEntityBuilder url(String url) {
            this.url = url;
            return this;
        }

        public MenuEntityBuilder component(String component) {
            this.component = component;
            return this;
        }

        public MenuEntityBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public MenuEntityBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public MenuEntityBuilder orderNum(Integer orderNum) {
            this.orderNum = orderNum;
            return this;
        }

        public MenuEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public MenuEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public MenuEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public MenuEntity build() {
            return new MenuEntity(menuId, parentId, title, name, url, component, type, icon, orderNum, status, created, updated);
        }
    }
}
