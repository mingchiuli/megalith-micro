package wiki.chiu.micro.user.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.service.UserRoleService;

/**
 * @Author limingjiu @Date 2024/5/29 22:12
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

  private final RoleRepository roleRepository;

  private final UserRoleRepository userRoleRepository;

  private final RoleDataPermissionRepository roleDataPermissionRepository;

  public UserRoleServiceImpl(
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository,
      RoleDataPermissionRepository roleDataPermissionRepository) {
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleDataPermissionRepository = roleDataPermissionRepository;
  }

  @Override
  public List<String> findRoleCodesByUserId(Long userId) {
    List<Long> roleIds =
        userRoleRepository.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();

    return roleRepository.findAllById(roleIds).stream()
        .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
        .map(RoleEntity::getCode)
        .toList();
  }

  @Override
  public List<DataPermissionEnum> findDataPermissionsByUserId(Long userId) {
    List<Long> roleIds =
        userRoleRepository.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();
    List<Long> enabledRoleIds =
        roleRepository.findAllById(roleIds).stream()
            .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
            .map(RoleEntity::getId)
            .toList();
    if (enabledRoleIds.isEmpty()) {
      return List.of();
    }
    return roleDataPermissionRepository.findByRoleIdIn(enabledRoleIds).stream()
        .map(wiki.chiu.micro.user.entity.RoleDataPermissionEntity::permission)
        .distinct()
        .sorted()
        .toList();
  }
}
