package wiki.chiu.micro.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = Const.ROLE_DATA_PERMISSION_TABLE,
    indexes = {@Index(columnList = "role_id")},
    uniqueConstraints = {@UniqueConstraint(columnNames = {"role_id", "permission_code"})})
public class RoleDataPermissionEntity {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "role_id", nullable = false)
  private Long roleId;

  @Column(name = "permission_code", nullable = false, length = 64)
  private String permissionCode;

  @Column(name = "created", updatable = false)
  @CreatedDate
  private LocalDateTime created;

  @Column(name = "updated")
  @LastModifiedDate
  private LocalDateTime updated;

  public RoleDataPermissionEntity() {}

  public RoleDataPermissionEntity(Long roleId, DataPermissionEnum permission) {
    this.roleId = roleId;
    this.permissionCode = permission.name();
  }

  public Long getId() {
    return id;
  }

  public Long getRoleId() {
    return roleId;
  }

  public String getPermissionCode() {
    return permissionCode;
  }

  public LocalDateTime getCreated() {
    return created;
  }

  public LocalDateTime getUpdated() {
    return updated;
  }

  public DataPermissionEnum permission() {
    return DataPermissionEnum.valueOf(permissionCode);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    RoleDataPermissionEntity that = (RoleDataPermissionEntity) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Hibernate.getClass(this).hashCode();
  }
}
