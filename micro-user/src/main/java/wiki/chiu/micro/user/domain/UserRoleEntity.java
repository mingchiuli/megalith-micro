package wiki.chiu.micro.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wiki.chiu.micro.common.lang.Const;

/**
 * @Author limingjiu @Date 2024/5/29 13:37
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = Const.USER_ROLE_TABLE,
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

  @Column(name = "created", updatable = false)
  @CreatedDate
  private LocalDateTime created;

  @Column(name = "updated")
  @LastModifiedDate
  private LocalDateTime updated;

  public UserRoleEntity(
      Long id, Long userId, Long roleId, LocalDateTime created, LocalDateTime updated) {
    this.id = id;
    this.userId = userId;
    this.roleId = roleId;
    this.created = created;
    this.updated = updated;
  }

  public UserRoleEntity() {}

  public static UserRoleEntityBuilder builder() {
    return new UserRoleEntityBuilder();
  }

  public Long getId() {
    return this.id;
  }

  public Long getUserId() {
    return this.userId;
  }

  public Long getRoleId() {
    return this.roleId;
  }

  public LocalDateTime getCreated() {
    return this.created;
  }

  public LocalDateTime getUpdated() {
    return this.updated;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public void setCreated(LocalDateTime created) {
    this.created = created;
  }

  public void setUpdated(LocalDateTime updated) {
    this.updated = updated;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    UserRoleEntity that = (UserRoleEntity) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Hibernate.getClass(this).hashCode();
  }

  public static class UserRoleEntityBuilder {
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime created;
    private LocalDateTime updated;

    UserRoleEntityBuilder() {}

    public UserRoleEntityBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public UserRoleEntityBuilder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public UserRoleEntityBuilder roleId(Long roleId) {
      this.roleId = roleId;
      return this;
    }

    public UserRoleEntityBuilder created(LocalDateTime created) {
      this.created = created;
      return this;
    }

    public UserRoleEntityBuilder updated(LocalDateTime updated) {
      this.updated = updated;
      return this;
    }

    public UserRoleEntity build() {
      return new UserRoleEntity(id, userId, roleId, created, updated);
    }
  }
}
