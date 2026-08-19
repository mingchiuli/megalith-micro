package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.user.entity.RoleDataPermissionEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.RoleDataPermissionService;
import wiki.chiu.micro.user.wrapper.RoleDataPermissionWrapper;

@Service
public class RoleDataPermissionServiceImpl implements RoleDataPermissionService {

  private final RoleRepository roleRepository;
  private final RoleDataPermissionRepository roleDataPermissionRepository;
  private final RoleDataPermissionWrapper roleDataPermissionWrapper;

  public RoleDataPermissionServiceImpl(
      RoleRepository roleRepository,
      RoleDataPermissionRepository roleDataPermissionRepository,
      RoleDataPermissionWrapper roleDataPermissionWrapper) {
    this.roleRepository = roleRepository;
    this.roleDataPermissionRepository = roleDataPermissionRepository;
    this.roleDataPermissionWrapper = roleDataPermissionWrapper;
  }

  @Override
  public List<DataPermissionEnum> getDataPermissions(Long roleId) {
    requireRole(roleId);
    return roleDataPermissionRepository.findByRoleId(roleId).stream()
        .map(RoleDataPermissionEntity::permission)
        .distinct()
        .sorted()
        .toList();
  }

  @Override
  public void saveDataPermissions(Long roleId, List<DataPermissionEnum> dataPermissions) {
    requireRole(roleId);
    List<RoleDataPermissionEntity> entities =
        dataPermissions.stream()
            .distinct()
            .sorted()
            .map(permission -> new RoleDataPermissionEntity(roleId, permission))
            .toList();
    roleDataPermissionWrapper.saveDataPermissions(roleId, entities);
  }

  private void requireRole(Long roleId) {
    roleRepository.findById(roleId).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
  }
}
