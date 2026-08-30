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
 * @Author limingjiu @Date 2024/2/21 19:13
 */
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = Const.MENU_AUTHORITY_TABLE,
    indexes = {@Index(columnList = "menu_id"), @Index(columnList = "authority_id")})
public class MenuAuthorityEntity {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "menu_id")
  private Long menuId;

  @Column(name = "authority_id")
  private Long authorityId;

  @Column(name = "created", updatable = false)
  @CreatedDate
  private LocalDateTime created;

  @Column(name = "updated")
  @LastModifiedDate
  private LocalDateTime updated;

  public MenuAuthorityEntity(
      Long id, Long menuId, Long authorityId, LocalDateTime created, LocalDateTime updated) {
    this.id = id;
    this.menuId = menuId;
    this.authorityId = authorityId;
    this.created = created;
    this.updated = updated;
  }

  public MenuAuthorityEntity() {}

  public static RoleAuthorityEntityBuilder builder() {
    return new RoleAuthorityEntityBuilder();
  }

  public Long getId() {
    return this.id;
  }

  public Long getMenuId() {
    return this.menuId;
  }

  public Long getAuthorityId() {
    return this.authorityId;
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

  public void setMenuId(Long menuId) {
    this.menuId = menuId;
  }

  public void setAuthorityId(Long authorityId) {
    this.authorityId = authorityId;
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
    MenuAuthorityEntity that = (MenuAuthorityEntity) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Hibernate.getClass(this).hashCode();
  }

  public static class RoleAuthorityEntityBuilder {
    private Long id;
    private Long menuId;
    private Long authorityId;
    private LocalDateTime created;
    private LocalDateTime updated;

    RoleAuthorityEntityBuilder() {}

    public RoleAuthorityEntityBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public RoleAuthorityEntityBuilder menuId(Long menuId) {
      this.menuId = menuId;
      return this;
    }

    public RoleAuthorityEntityBuilder authorityId(Long authorityId) {
      this.authorityId = authorityId;
      return this;
    }

    public RoleAuthorityEntityBuilder created(LocalDateTime created) {
      this.created = created;
      return this;
    }

    public RoleAuthorityEntityBuilder updated(LocalDateTime updated) {
      this.updated = updated;
      return this;
    }

    public MenuAuthorityEntity build() {
      return new MenuAuthorityEntity(id, menuId, authorityId, created, updated);
    }
  }
}
