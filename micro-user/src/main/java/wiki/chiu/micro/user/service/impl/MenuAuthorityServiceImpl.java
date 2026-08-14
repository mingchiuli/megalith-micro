package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.convertor.MenuAuthorityEntityConvertor;
import wiki.chiu.micro.user.convertor.MenuAuthorityVoConvertor;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;
import wiki.chiu.micro.user.wrapper.MenuAuthorityWrapper;

@Service
public class MenuAuthorityServiceImpl implements MenuAuthorityService {

  private final MenuAuthorityWrapper menuAuthorityWrapper;

  private final MenuAuthorityRepository menuAuthorityRepository;

  private final AuthorityRepository authorityRepository;

  public MenuAuthorityServiceImpl(
      MenuAuthorityWrapper menuAuthorityWrapper,
      MenuAuthorityRepository menuAuthorityRepository,
      AuthorityRepository authorityRepository) {
    this.menuAuthorityWrapper = menuAuthorityWrapper;
    this.menuAuthorityRepository = menuAuthorityRepository;
    this.authorityRepository = authorityRepository;
  }

  @Override
  public void saveAuthority(Long menuId, List<Long> authorityIds) {
    List<MenuAuthorityEntity> roleAuthorityEntities =
        MenuAuthorityEntityConvertor.convert(menuId, authorityIds);
    menuAuthorityWrapper.saveAuthority(menuId, roleAuthorityEntities);
  }

  @Override
  public List<MenuAuthorityVo> getAuthoritiesInfo(Long menuId) {
    List<Long> ids =
        menuAuthorityRepository.findByMenuId(menuId).stream()
            .map(MenuAuthorityEntity::getAuthorityId)
            .toList();

    return authorityRepository.findAll().stream()
        .filter(item -> NORMAL.getCode().equals(item.getStatus()))
        .filter(item -> AuthTypeEnum.NEED_AUTH.getCode().equals(item.getType()))
        .map(item -> MenuAuthorityVoConvertor.convert(item, ids))
        .toList();
  }
}
