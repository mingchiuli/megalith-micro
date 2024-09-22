package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleMenuEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(roleId);
        result = 31 * result + Objects.hashCode(menuId);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
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
            return new RoleMenuEntity(id, roleId, menuId, created, updated);
        }
    }
}
