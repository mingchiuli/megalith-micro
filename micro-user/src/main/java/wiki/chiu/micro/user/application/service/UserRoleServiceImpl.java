package wiki.chiu.micro.user.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.application.port.in.UserRoleService;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

/**
 * @Author limingjiu @Date 2024/5/29 22:12
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

  private final RoleReader roleRepository;

  private final UserRoleReader userRoleReader;

  private final RoleDataPermissionReader roleDataPermissionRepository;

  public UserRoleServiceImpl(
      RoleReader roleRepository,
      UserRoleReader userRoleReader,
      RoleDataPermissionReader roleDataPermissionRepository) {
    this.roleRepository = roleRepository;
    this.userRoleReader = userRoleReader;
    this.roleDataPermissionRepository = roleDataPermissionRepository;
  }

  @Override
  public List<String> findRoleCodesByUserId(Long userId) {
    List<Long> roleIds =
        userRoleReader.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();

    return roleRepository.findAllById(roleIds).stream()
        .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
        .map(RoleEntity::getCode)
        .toList();
  }

  @Override
  public List<DataPermissionEnum> findDataPermissionsByUserId(Long userId) {
    List<Long> roleIds =
        userRoleReader.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();
    List<Long> enabledRoleIds =
        roleRepository.findAllById(roleIds).stream()
            .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
            .map(RoleEntity::getId)
            .toList();
    if (enabledRoleIds.isEmpty()) {
      return List.of();
    }
    return roleDataPermissionRepository.findByRoleIdIn(enabledRoleIds).stream()
        .map(wiki.chiu.micro.user.domain.RoleDataPermissionEntity::permission)
        .distinct()
        .sorted()
        .toList();
  }
}
