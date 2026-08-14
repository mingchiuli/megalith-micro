package wiki.chiu.micro.user.wrapper;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class MenuAuthorityWrapper {

  private final MenuAuthorityRepository menuAuthorityRepository;

  private final AuthorityRepository authorityRepository;
  private final RoleRepository roleRepository;
  private final AuthCacheEvictionOutbox cacheEvictions;

  public MenuAuthorityWrapper(
      MenuAuthorityRepository menuAuthorityRepository,
      AuthorityRepository authorityRepository,
      RoleRepository roleRepository,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.menuAuthorityRepository = menuAuthorityRepository;
    this.authorityRepository = authorityRepository;
    this.roleRepository = roleRepository;
    this.cacheEvictions = cacheEvictions;
  }

  @Transactional
  public void saveAuthority(Long menuId, List<MenuAuthorityEntity> menuAuthorityEntities) {
    menuAuthorityRepository.deleteByMenuId(menuId);
    menuAuthorityRepository.saveAll(menuAuthorityEntities);
    enqueueRoleEviction(false);
  }

  @Transactional
  public void deleteAuthorities(List<Long> ids) {
    authorityRepository.deleteAllById(ids);
    menuAuthorityRepository.deleteByAuthorityIdIn(ids);
    enqueueRoleEviction(true);
  }

  @Transactional
  public void authorityEntitySave(AuthorityEntity authorityEntity) {
    Long authorityId = authorityEntity.getId();
    if (authorityId != null
        && AuthTypeEnum.WHITE_LIST.getCode().equals(authorityEntity.getType())) {
      menuAuthorityRepository.deleteByAuthorityId(authorityId);
    }
    authorityRepository.save(authorityEntity);
    enqueueRoleEviction(true);
  }

  private void enqueueRoleEviction(boolean evictRoutes) {
    cacheEvictions.enqueue(
        List.of(),
        roleRepository.findAll().stream().map(RoleEntity::getId).toList(),
        List.of(),
        false,
        evictRoutes);
  }
}
