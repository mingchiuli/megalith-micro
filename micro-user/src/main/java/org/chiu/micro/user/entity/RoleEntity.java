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
 * @create 2022-11-27 11:23 am
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_role",
        indexes = {@Index(columnList = "created")},
        uniqueConstraints = {@UniqueConstraint(columnNames = "code"), @UniqueConstraint(columnNames = "name")})
public class RoleEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Column(name = "status")
    private Integer status;

    public RoleEntity(Long id, String name, String code, String remark, LocalDateTime created, LocalDateTime updated, Integer status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.remark = remark;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    public RoleEntity() {
    }

    public static RoleEntityBuilder builder() {
        return new RoleEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(code, that.code) && Objects.equals(remark, that.remark) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(code);
        result = 31 * result + Objects.hashCode(remark);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        result = 31 * result + Objects.hashCode(status);
        return result;
    }

    public static class RoleEntityBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        RoleEntityBuilder() {
        }

        public RoleEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleEntityBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RoleEntityBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public RoleEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RoleEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public RoleEntity build() {
            return new RoleEntity(id, name, code, remark, created, updated, status);
        }
    }
}
