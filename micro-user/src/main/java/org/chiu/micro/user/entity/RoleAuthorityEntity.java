package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Objects;

/**
 * @Author limingjiu
 * @Date 2024/2/21 19:13
 **/
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicUpdate
@Table(name ="m_role_authority",
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleAuthorityEntity that = (RoleAuthorityEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(roleId, that.roleId)) return false;
        return Objects.equals(authorityId, that.authorityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
