package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name ="m_authority",
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthorityEntity that = (AuthorityEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(code, that.code)) return false;
        if (!Objects.equals(remark, that.remark)) return false;
        if (!Objects.equals(created, that.created)) return false;
        if (!Objects.equals(updated, that.updated)) return false;
        if (!Objects.equals(prototype, that.prototype)) return false;
        if (!Objects.equals(methodType, that.methodType)) return false;
        if (!Objects.equals(serviceHost, that.serviceHost)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
