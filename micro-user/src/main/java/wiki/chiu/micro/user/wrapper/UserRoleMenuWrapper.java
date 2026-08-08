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

  public UserRoleMenuWrapper(
      RoleRepository roleRepository,
      RoleMenuRepository roleMenuRepository,
      UserRoleRepository userRoleRepository) {
    this.roleRepository = roleRepository;
    this.roleMenuRepository = roleMenuRepository;
    this.userRoleRepository = userRoleRepository;
  }

  @Transactional
  public void deleteRole(List<Long> ids) {
    roleMenuRepository.deleteAllByRoleIdIn(ids);
    userRoleRepository.deleteByRoleIdIn(ids);
    roleRepository.deleteAllById(ids);
  }
}
