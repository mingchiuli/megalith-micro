package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionWriter;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleDataPermissionWrapper implements RoleDataPermissionWriter {

  private final RoleDataPermissionRepository roleDataPermissions;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleDataPermissionWrapper(
      RoleDataPermissionRepository roleDataPermissions, AuthCacheEvictionOutbox cacheEvictions) {
    this.roleDataPermissions = roleDataPermissions;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  @Override
  public void saveDataPermissions(
      Long roleId, List<RoleDataPermissionEntity> dataPermissionEntities) {
    roleDataPermissions.deleteByRoleId(roleId);
    roleDataPermissions.flush();
    roleDataPermissions.saveAll(dataPermissionEntities);
    cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(), false, false);
  }
}
