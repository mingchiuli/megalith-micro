package org.chiu.micro.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import java.util.Objects;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:45 am
 */

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
@DynamicUpdate
@Table(name ="m_role_menu",
        indexes = {@Index(columnList = "role_id"), @Index(columnList = "menu_id")})
public class RoleMenuEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "menu_id")
    private Long menuId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleMenuEntity that = (RoleMenuEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(roleId, that.roleId)) return false;
        return Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
