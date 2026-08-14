package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.*;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleMenuWrapper {

  private final RoleMenuRepository roleMenuRepository;
  private final RoleRepository roleRepository;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleMenuWrapper(
      RoleMenuRepository roleMenuRepository,
      RoleRepository roleRepository,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.roleMenuRepository = roleMenuRepository;
    this.roleRepository = roleRepository;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveMenu(Long roleId, List<RoleMenuEntity> roleMenuEntities) {
    roleMenuRepository.deleteByRoleId(roleId);
    roleMenuRepository.saveAll(roleMenuEntities);
    roleRepository
        .findById(roleId)
        .ifPresent(
            role ->
                cacheEvictions.enqueue(
                    List.of(), List.of(roleId), List.of(role.getCode()), true, false));
  }
}
