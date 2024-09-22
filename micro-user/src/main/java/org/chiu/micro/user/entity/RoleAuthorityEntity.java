package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/2/21 19:13
 **/
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_role_authority",
        indexes = {@Index(columnList = "role_id"), @Index(columnList = "authority_id")})
public class RoleAuthorityEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "authority_id")
    private Long authorityId;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    public RoleAuthorityEntity(Long id, Long roleId, Long authorityId, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.roleId = roleId;
        this.authorityId = authorityId;
        this.created = created;
        this.updated = updated;
    }

    public RoleAuthorityEntity() {
    }

    public static RoleAuthorityEntityBuilder builder() {
        return new RoleAuthorityEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public Long getAuthorityId() {
        return this.authorityId;
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

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleAuthorityEntity)) return false;
        final RoleAuthorityEntity other = (RoleAuthorityEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$roleId = this.getRoleId();
        final Object other$roleId = other.getRoleId();
        if (this$roleId == null ? other$roleId != null : !this$roleId.equals(other$roleId)) return false;
        final Object this$authorityId = this.getAuthorityId();
        final Object other$authorityId = other.getAuthorityId();
        if (this$authorityId == null ? other$authorityId != null : !this$authorityId.equals(other$authorityId))
            return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RoleAuthorityEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $roleId = this.getRoleId();
        result = result * PRIME + ($roleId == null ? 43 : $roleId.hashCode());
        final Object $authorityId = this.getAuthorityId();
        result = result * PRIME + ($authorityId == null ? 43 : $authorityId.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        return result;
    }

    public String toString() {
        return "RoleAuthorityEntity(id=" + this.getId() + ", roleId=" + this.getRoleId() + ", authorityId=" + this.getAuthorityId() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ")";
    }

    public static class RoleAuthorityEntityBuilder {
        private Long id;
        private Long roleId;
        private Long authorityId;
        private LocalDateTime created;
        private LocalDateTime updated;

        RoleAuthorityEntityBuilder() {
        }

        public RoleAuthorityEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleAuthorityEntityBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public RoleAuthorityEntityBuilder authorityId(Long authorityId) {
            this.authorityId = authorityId;
            return this;
        }

        public RoleAuthorityEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RoleAuthorityEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleAuthorityEntity build() {
            return new RoleAuthorityEntity(this.id, this.roleId, this.authorityId, this.created, this.updated);
        }

        public String toString() {
            return "RoleAuthorityEntity.RoleAuthorityEntityBuilder(id=" + this.id + ", roleId=" + this.roleId + ", authorityId=" + this.authorityId + ", created=" + this.created + ", updated=" + this.updated + ")";
        }
    }
}
