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
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleMenuWrapper(
      RoleMenuRepository roleMenuRepository, AuthCacheEvictionOutbox cacheEvictions) {
    this.roleMenuRepository = roleMenuRepository;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveMenu(Long roleId, String roleCode, List<RoleMenuEntity> roleMenuEntities) {
    roleMenuRepository.deleteByRoleId(roleId);
    roleMenuRepository.saveAll(roleMenuEntities);
    cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(roleCode), true, false);
  }
}
