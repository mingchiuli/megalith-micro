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

    @Column(name = "type")
    private Integer type;

    @Column(name = "status")
    private Integer status;

    public AuthorityEntity(Long id, String name, String code, String remark, String prototype, String methodType, String routePattern, String serviceHost, Integer servicePort, LocalDateTime created, LocalDateTime updated, Integer type, Integer status) {
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
        this.type = type;
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

    public Integer getType() {
        return type;
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

    public void setType(Integer type) {
        this.type = type;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorityEntity that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(code, that.code) && Objects.equals(remark, that.remark) && Objects.equals(prototype, that.prototype) && Objects.equals(methodType, that.methodType) && Objects.equals(routePattern, that.routePattern) && Objects.equals(serviceHost, that.serviceHost) && Objects.equals(servicePort, that.servicePort) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(code);
        result = 31 * result + Objects.hashCode(remark);
        result = 31 * result + Objects.hashCode(prototype);
        result = 31 * result + Objects.hashCode(methodType);
        result = 31 * result + Objects.hashCode(routePattern);
        result = 31 * result + Objects.hashCode(serviceHost);
        result = 31 * result + Objects.hashCode(servicePort);
        result = 31 * result + Objects.hashCode(created);
        result = 31 * result + Objects.hashCode(updated);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(type);
        return result;
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
        private Integer type;
        private Integer status;


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

        public AuthorityEntityBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public AuthorityEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public AuthorityEntity build() {
            return new AuthorityEntity(id, name, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }
    }
}
