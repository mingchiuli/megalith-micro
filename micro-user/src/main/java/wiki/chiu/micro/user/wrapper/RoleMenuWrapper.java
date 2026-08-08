package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.*;

@Component
public class RoleMenuWrapper {

  private final RoleMenuRepository roleMenuRepository;

  public RoleMenuWrapper(RoleMenuRepository roleMenuRepository) {
    this.roleMenuRepository = roleMenuRepository;
  }

  @Transactional
  public void saveMenu(Long roleId, List<RoleMenuEntity> roleMenuEntities) {
    roleMenuRepository.deleteByRoleId(roleId);
    roleMenuRepository.saveAll(roleMenuEntities);
  }
}
