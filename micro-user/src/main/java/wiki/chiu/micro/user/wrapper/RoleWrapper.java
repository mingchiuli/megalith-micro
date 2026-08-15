package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleWrapper {

  private final RoleRepository roles;
  private final RoleMenuRepository roleMenus;
  private final UserRoleRepository userRoles;
  private final RoleDataPermissionRepository dataPermissions;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleWrapper(
      RoleRepository roles,
      RoleMenuRepository roleMenus,
      UserRoleRepository userRoles,
      RoleDataPermissionRepository dataPermissions,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.roles = roles;
    this.roleMenus = roleMenus;
    this.userRoles = userRoles;
    this.dataPermissions = dataPermissions;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveOrUpdate(
      RoleEntity role, List<DataPermissionEnum> permissions, List<String> affectedCodes) {
    RoleEntity saved = roles.save(role);
    dataPermissions.deleteByRoleId(saved.getId());
    dataPermissions.flush();
    dataPermissions.saveAll(
        permissions.stream()
            .distinct()
            .sorted()
            .map(permission -> new RoleDataPermissionEntity(saved.getId(), permission))
            .toList());
    cacheEvictions.enqueue(List.of(), List.of(saved.getId()), affectedCodes, true, false);
  }

  @Transactional
  public void delete(List<Long> ids, List<String> roleCodes) {
    roleMenus.deleteAllByRoleIdIn(ids);
    userRoles.deleteByRoleIdIn(ids);
    dataPermissions.deleteByRoleIdIn(ids);
    roles.deleteAllByIdInBatch(ids);
    cacheEvictions.enqueue(List.of(), ids, roleCodes, true, false);
  }
}
