package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Objects;

/**
 * @Author limingjiu
 * @Date 2024/5/29 13:37
 **/
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
@DynamicUpdate
@Table(name ="m_user_role",
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoleEntity that = (UserRoleEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(roleId, that.roleId)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
