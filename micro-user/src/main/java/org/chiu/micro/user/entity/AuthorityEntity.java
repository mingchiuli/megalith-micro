package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/2/21 19:12
 **/
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_authority",
        uniqueConstraints = {@UniqueConstraint(columnNames = "code")}, indexes = {@Index(columnList = "service_host")})
public class AuthorityEntity {

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

    @Column(name = "prototype")
    private String prototype;

    @Column(name = "method_type")
    private String methodType;

    @Column(name = "route_pattern")
    private String routePattern;

    @Column(name = "service_host")
    private String serviceHost;

    @Column(name = "service_port")
    private Integer servicePort;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Column(name = "status")
    private Integer status;

    public AuthorityEntity(Long id, String name, String code, String remark, String prototype, String methodType, String routePattern, String serviceHost, Integer servicePort, LocalDateTime created, LocalDateTime updated, Integer status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.remark = remark;
        this.prototype = prototype;
        this.methodType = methodType;
        this.routePattern = routePattern;
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    public AuthorityEntity() {
    }

    public static AuthorityEntityBuilder builder() {
        return new AuthorityEntityBuilder();
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

    public String getPrototype() {
        return this.prototype;
    }

    public String getMethodType() {
        return this.methodType;
    }

    public String getRoutePattern() {
        return this.routePattern;
    }

    public String getServiceHost() {
        return this.serviceHost;
    }

    public Integer getServicePort() {
        return this.servicePort;
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

    public void setPrototype(String prototype) {
        this.prototype = prototype;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public void setRoutePattern(String routePattern) {
        this.routePattern = routePattern;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorityEntity)) return false;
        final AuthorityEntity other = (AuthorityEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final Object this$remark = this.getRemark();
        final Object other$remark = other.getRemark();
        if (this$remark == null ? other$remark != null : !this$remark.equals(other$remark)) return false;
        final Object this$prototype = this.getPrototype();
        final Object other$prototype = other.getPrototype();
        if (this$prototype == null ? other$prototype != null : !this$prototype.equals(other$prototype)) return false;
        final Object this$methodType = this.getMethodType();
        final Object other$methodType = other.getMethodType();
        if (this$methodType == null ? other$methodType != null : !this$methodType.equals(other$methodType))
            return false;
        final Object this$routePattern = this.getRoutePattern();
        final Object other$routePattern = other.getRoutePattern();
        if (this$routePattern == null ? other$routePattern != null : !this$routePattern.equals(other$routePattern))
            return false;
        final Object this$serviceHost = this.getServiceHost();
        final Object other$serviceHost = other.getServiceHost();
        if (this$serviceHost == null ? other$serviceHost != null : !this$serviceHost.equals(other$serviceHost))
            return false;
        final Object this$servicePort = this.getServicePort();
        final Object other$servicePort = other.getServicePort();
        if (this$servicePort == null ? other$servicePort != null : !this$servicePort.equals(other$servicePort))
            return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorityEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $remark = this.getRemark();
        result = result * PRIME + ($remark == null ? 43 : $remark.hashCode());
        final Object $prototype = this.getPrototype();
        result = result * PRIME + ($prototype == null ? 43 : $prototype.hashCode());
        final Object $methodType = this.getMethodType();
        result = result * PRIME + ($methodType == null ? 43 : $methodType.hashCode());
        final Object $routePattern = this.getRoutePattern();
        result = result * PRIME + ($routePattern == null ? 43 : $routePattern.hashCode());
        final Object $serviceHost = this.getServiceHost();
        result = result * PRIME + ($serviceHost == null ? 43 : $serviceHost.hashCode());
        final Object $servicePort = this.getServicePort();
        result = result * PRIME + ($servicePort == null ? 43 : $servicePort.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "AuthorityEntity(id=" + this.getId() + ", name=" + this.getName() + ", code=" + this.getCode() + ", remark=" + this.getRemark() + ", prototype=" + this.getPrototype() + ", methodType=" + this.getMethodType() + ", routePattern=" + this.getRoutePattern() + ", serviceHost=" + this.getServiceHost() + ", servicePort=" + this.getServicePort() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ", status=" + this.getStatus() + ")";
    }

    public static class AuthorityEntityBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private String prototype;
        private String methodType;
        private String routePattern;
        private String serviceHost;
        private Integer servicePort;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        AuthorityEntityBuilder() {
        }

        public AuthorityEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuthorityEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AuthorityEntityBuilder code(String code) {
            this.code = code;
            return this;
        }

        public AuthorityEntityBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public AuthorityEntityBuilder prototype(String prototype) {
            this.prototype = prototype;
            return this;
        }

        public AuthorityEntityBuilder methodType(String methodType) {
            this.methodType = methodType;
            return this;
        }

        public AuthorityEntityBuilder routePattern(String routePattern) {
            this.routePattern = routePattern;
            return this;
        }

        public AuthorityEntityBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityEntityBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityEntityBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public AuthorityEntityBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public AuthorityEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public AuthorityEntity build() {
            return new AuthorityEntity(this.id, this.name, this.code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.created, this.updated, this.status);
        }

        public String toString() {
            return "AuthorityEntity.AuthorityEntityBuilder(id=" + this.id + ", name=" + this.name + ", code=" + this.code + ", remark=" + this.remark + ", prototype=" + this.prototype + ", methodType=" + this.methodType + ", routePattern=" + this.routePattern + ", serviceHost=" + this.serviceHost + ", servicePort=" + this.servicePort + ", created=" + this.created + ", updated=" + this.updated + ", status=" + this.status + ")";
        }
    }
}
