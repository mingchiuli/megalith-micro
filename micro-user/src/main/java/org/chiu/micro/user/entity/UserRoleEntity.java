package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(userId);
        result = 31 * result + Objects.hashCode(roleId);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
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
            return new UserRoleEntity(id, userId, roleId, created, updated);
        }
    }
}
