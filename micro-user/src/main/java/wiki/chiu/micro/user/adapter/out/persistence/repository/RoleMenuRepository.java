package wiki.chiu.micro.user.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.domain.RoleMenuEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Long>, RoleMenuReader {

    @Query(value = "SELECT roleMenu.menuId from RoleMenuEntity roleMenu where roleMenu.roleId = ?1")
    List<Long> findMenuIdsByRoleId(Long id);

    List<RoleMenuEntity> findByRoleIdIn(List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RoleMenuEntity roleMenu WHERE roleMenu.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RoleMenuEntity roleMenu WHERE roleMenu.menuId = :menuId")
    void deleteByMenuId(@Param("menuId") Long menuId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RoleMenuEntity roleMenu WHERE roleMenu.roleId IN :roleIds")
    void deleteByRoleIdIn(@Param("roleIds") List<Long> roleIds);
}
