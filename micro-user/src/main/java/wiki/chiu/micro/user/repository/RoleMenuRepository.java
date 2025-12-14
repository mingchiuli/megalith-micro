package wiki.chiu.micro.user.repository;

import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Transactional
    void deleteByRoleId(Long roleId);

    @Transactional
    void deleteByMenuId(Long menuId);

    @Transactional
    void deleteAllByRoleIdIn(List<Long> ids);

    List<RoleMenuEntity> findByRoleId(Long roleId);
}
