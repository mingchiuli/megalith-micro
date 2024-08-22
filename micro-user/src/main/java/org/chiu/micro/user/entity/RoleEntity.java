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
 * @author mingchiuli
 * @create 2022-11-27 11:23 am
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name ="m_role",
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleEntity that = (RoleEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(code, that.code)) return false;
        if (!Objects.equals(remark, that.remark)) return false;
        if (!Objects.equals(created, that.created)) return false;
        if (!Objects.equals(updated, that.updated)) return false;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
