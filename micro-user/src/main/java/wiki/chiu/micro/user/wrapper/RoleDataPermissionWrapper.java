package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleDataPermissionWrapper {

  private final RoleDataPermissionRepository roleDataPermissions;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleDataPermissionWrapper(
      RoleDataPermissionRepository roleDataPermissions, AuthCacheEvictionOutbox cacheEvictions) {
    this.roleDataPermissions = roleDataPermissions;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveDataPermissions(
      Long roleId, List<RoleDataPermissionEntity> dataPermissionEntities) {
    roleDataPermissions.deleteByRoleId(roleId);
    roleDataPermissions.flush();
    roleDataPermissions.saveAll(dataPermissionEntities);
    cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(), false, false);
  }
}
