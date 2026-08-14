package wiki.chiu.micro.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wiki.chiu.micro.user.entity.RoleMenuEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 11:53 am
 */
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Long> {

  @Query(value = "SELECT roleMenu.menuId from RoleMenuEntity roleMenu where roleMenu.roleId = ?1")
  List<Long> findMenuIdsByRoleId(Long id);

  @Query(
      value = "SELECT roleMenu.menuId from RoleMenuEntity roleMenu where roleMenu.roleId in (?1)")
  List<Long> findMenuIdsByRoleIdIn(List<Long> ids);

  List<RoleMenuEntity> findByRoleIdIn(List<Long> ids);

  void deleteByRoleId(Long roleId);

  void deleteByMenuId(Long menuId);

  void deleteAllByRoleIdIn(List<Long> ids);
}
