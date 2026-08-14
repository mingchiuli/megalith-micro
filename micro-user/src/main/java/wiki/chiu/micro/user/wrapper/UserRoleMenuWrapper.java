package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.repository.*;

@Component
public class UserRoleMenuWrapper {

  private final RoleRepository roleRepository;

  private final RoleMenuRepository roleMenuRepository;

  private final UserRoleRepository userRoleRepository;

  private final RoleDataPermissionRepository roleDataPermissionRepository;

  public UserRoleMenuWrapper(
      RoleRepository roleRepository,
      RoleMenuRepository roleMenuRepository,
      UserRoleRepository userRoleRepository,
      RoleDataPermissionRepository roleDataPermissionRepository) {
    this.roleRepository = roleRepository;
    this.roleMenuRepository = roleMenuRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleDataPermissionRepository = roleDataPermissionRepository;
  }

  @Transactional
  public void deleteRole(List<Long> ids) {
    roleMenuRepository.deleteAllByRoleIdIn(ids);
    userRoleRepository.deleteByRoleIdIn(ids);
    roleDataPermissionRepository.deleteByRoleIdIn(ids);
    roleRepository.deleteAllById(ids);
  }
}
