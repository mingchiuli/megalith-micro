package org.chiu.micro.user.repository;

import org.chiu.micro.user.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Long> {

    @Query(value = "SELECT roleMenu.menuId from RoleMenuEntity roleMenu where roleMenu.roleId = ?1")
    List<Long> findMenuIdsByRoleId(Long id);

    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);

    @Modifying
    @Transactional
    void deleteByMenuId(Long menuId);

    @Query(value = "DELETE from RoleMenuEntity roleMenu where roleMenu.roleId in (?1)")
    @Modifying
    @Transactional
    void deleteAllByRoleId(List<Long> ids);
}
