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
 * @Author limingjiu
 * @Date 2024/2/21 19:13
 **/
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = Const.ROLE_AUTHORITY_TABLE,
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleAuthorityEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(roleId, that.roleId) && Objects.equals(authorityId, that.authorityId) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(roleId);
        result = 31 * result + Objects.hashCode(authorityId);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        return result;
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
            return new RoleAuthorityEntity(id, roleId, authorityId, created, updated);
        }
    }
}
