package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
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
}
