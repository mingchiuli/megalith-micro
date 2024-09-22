package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/5/29 13:37
 **/
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_user_role",
        indexes = {@Index(columnList = "user_id"), @Index(columnList = "role_id")})
public class UserRoleEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    public UserRoleEntity(Long id, Long userId, Long roleId, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.created = created;
        this.updated = updated;
    }

    public UserRoleEntity() {
    }

    public static UserRoleEntityBuilder builder() {
        return new UserRoleEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getRoleId() {
        return this.roleId;
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

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserRoleEntity)) return false;
        final UserRoleEntity other = (UserRoleEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$roleId = this.getRoleId();
        final Object other$roleId = other.getRoleId();
        if (this$roleId == null ? other$roleId != null : !this$roleId.equals(other$roleId)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserRoleEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $roleId = this.getRoleId();
        result = result * PRIME + ($roleId == null ? 43 : $roleId.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        return result;
    }

    public String toString() {
        return "UserRoleEntity(id=" + this.getId() + ", userId=" + this.getUserId() + ", roleId=" + this.getRoleId() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ")";
    }

    public static class UserRoleEntityBuilder {
        private Long id;
        private Long userId;
        private Long roleId;
        private LocalDateTime created;
        private LocalDateTime updated;

        UserRoleEntityBuilder() {
        }

        public UserRoleEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserRoleEntityBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserRoleEntityBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserRoleEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public UserRoleEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public UserRoleEntity build() {
            return new UserRoleEntity(this.id, this.userId, this.roleId, this.created, this.updated);
        }

        public String toString() {
            return "UserRoleEntity.UserRoleEntityBuilder(id=" + this.id + ", userId=" + this.userId + ", roleId=" + this.roleId + ", created=" + this.created + ", updated=" + this.updated + ")";
        }
    }
}
