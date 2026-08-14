package wiki.chiu.micro.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.user.entity.RoleEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:52 am
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

  interface RoleAuthorizationRow {
    Long getRoleId();

    String getCode();

    Integer getStatus();

    String getAuthorityCode();

    String getPermissionCode();
  }

  @Query(
      value =
          """
          SELECT r.id AS roleId,
                 r.code AS code,
                 r.status AS status,
                 authority.code AS authorityCode,
                 data_permission.permission_code AS permissionCode
          FROM m_role r
          LEFT JOIN m_role_data_permission data_permission
            ON data_permission.role_id = r.id
          LEFT JOIN m_role_menu role_menu ON role_menu.role_id = r.id
          LEFT JOIN m_menu_authority menu_authority
            ON menu_authority.menu_id = role_menu.menu_id
          LEFT JOIN m_authority authority
            ON authority.id = menu_authority.authority_id AND authority.status = 0
          WHERE r.id = :roleId
          """,
      nativeQuery = true)
  List<RoleAuthorizationRow> findAuthorizationRows(Long roleId);

  @Query(
      value =
          """
          SELECT r.id AS roleId,
                 r.code AS code,
                 r.status AS status,
                 authority.code AS authorityCode,
                 data_permission.permission_code AS permissionCode
          FROM m_role r
          LEFT JOIN m_role_data_permission data_permission
            ON data_permission.role_id = r.id
          LEFT JOIN m_role_menu role_menu ON role_menu.role_id = r.id
          LEFT JOIN m_menu_authority menu_authority
            ON menu_authority.menu_id = role_menu.menu_id
          LEFT JOIN m_authority authority
            ON authority.id = menu_authority.authority_id AND authority.status = 0
          WHERE r.id IN (:roleIds)
          """,
      nativeQuery = true)
  List<RoleAuthorizationRow> findAuthorizationRowsByRoleIds(List<Long> roleIds);

  List<RoleEntity> findByCodeIn(List<String> roles);

  List<RoleEntity> findByCodeInAndStatus(List<String> roles, Integer status);

  Optional<RoleEntity> findByCode(String role);

  @Query(value = "SELECT role.code from RoleEntity role")
  List<String> findAllCodes();
}
