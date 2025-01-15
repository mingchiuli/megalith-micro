package wiki.chiu.micro.user.repository;

import wiki.chiu.micro.user.entity.RoleMenuEntity;
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

    @Query(value = "SELECT roleMenu.menuId from RoleMenuEntity roleMenu where roleMenu.roleId in (?1)")
    List<Long> findMenuIdsByRoleIdIn(List<Long> ids);
    
    List<RoleMenuEntity> findByRoleIdIn(List<Long> ids);

    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);

    @Modifying
    @Transactional
    void deleteByMenuId(Long menuId);

    @Modifying
    @Transactional
    void deleteAllByRoleIdIn(List<Long> ids);

    List<RoleMenuEntity> findByRoleId(Long roleId);
}
