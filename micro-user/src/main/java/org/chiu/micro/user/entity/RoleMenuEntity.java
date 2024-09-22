package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:45 am
 */

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_role_menu",
        indexes = {@Index(columnList = "role_id"), @Index(columnList = "menu_id")})
public class RoleMenuEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    public RoleMenuEntity(Long id, Long roleId, Long menuId, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.roleId = roleId;
        this.menuId = menuId;
        this.created = created;
        this.updated = updated;
    }

    public RoleMenuEntity() {
    }

    public static RoleMenuEntityBuilder builder() {
        return new RoleMenuEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public Long getMenuId() {
        return this.menuId;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleMenuEntity)) return false;
        final RoleMenuEntity other = (RoleMenuEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$roleId = this.getRoleId();
        final Object other$roleId = other.getRoleId();
        if (this$roleId == null ? other$roleId != null : !this$roleId.equals(other$roleId)) return false;
        final Object this$menuId = this.getMenuId();
        final Object other$menuId = other.getMenuId();
        if (this$menuId == null ? other$menuId != null : !this$menuId.equals(other$menuId)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RoleMenuEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $roleId = this.getRoleId();
        result = result * PRIME + ($roleId == null ? 43 : $roleId.hashCode());
        final Object $menuId = this.getMenuId();
        result = result * PRIME + ($menuId == null ? 43 : $menuId.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        return result;
    }

    public String toString() {
        return "RoleMenuEntity(id=" + this.getId() + ", roleId=" + this.getRoleId() + ", menuId=" + this.getMenuId() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ")";
    }

    public static class RoleMenuEntityBuilder {
        private Long id;
        private Long roleId;
        private Long menuId;
        private LocalDateTime created;
        private LocalDateTime updated;

        RoleMenuEntityBuilder() {
        }

        public RoleMenuEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleMenuEntityBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public RoleMenuEntityBuilder menuId(Long menuId) {
            this.menuId = menuId;
            return this;
        }

        public RoleMenuEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RoleMenuEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleMenuEntity build() {
            return new RoleMenuEntity(this.id, this.roleId, this.menuId, this.created, this.updated);
        }

        public String toString() {
            return "RoleMenuEntity.RoleMenuEntityBuilder(id=" + this.id + ", roleId=" + this.roleId + ", menuId=" + this.menuId + ", created=" + this.created + ", updated=" + this.updated + ")";
        }
    }
}
