package wiki.chiu.micro.user.wrapper;

import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_INVALID_OPERATE;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleMenuAuthorityWrapper {

  private final MenuRepository menuRepository;

  private final MenuAuthorityRepository menuAuthorityRepository;

  private final RoleMenuRepository roleMenuRepository;
  private final RoleRepository roleRepository;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleMenuAuthorityWrapper(
      MenuRepository menuRepository,
      MenuAuthorityRepository menuAuthorityRepository,
      RoleMenuRepository roleMenuRepository,
      RoleRepository roleRepository,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.menuRepository = menuRepository;
    this.menuAuthorityRepository = menuAuthorityRepository;
    this.roleMenuRepository = roleMenuRepository;
    this.roleRepository = roleRepository;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveMenu(List<MenuEntity> menus) {
    menuRepository.saveAll(menus);
    enqueueAllRoleEviction();
  }

  @Transactional
  public void deleteMenu(Long id) {
    if (menuRepository.existsByParentId(id)) {
      throw new BaseException(MENU_INVALID_OPERATE);
    }
    menuRepository.deleteById(id);
    menuAuthorityRepository.deleteByMenuId(id);
    roleMenuRepository.deleteByMenuId(id);
    enqueueAllRoleEviction();
  }

  private void enqueueAllRoleEviction() {
    List<RoleEntity> roles = roleRepository.findAll();
    cacheEvictions.enqueue(
        List.of(),
        roles.stream().map(RoleEntity::getId).toList(),
        roles.stream().map(RoleEntity::getCode).toList(),
        true,
        false);
  }
}
