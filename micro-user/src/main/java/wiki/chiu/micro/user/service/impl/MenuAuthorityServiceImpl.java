package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.MenuAuthorityEntityConvertor;
import wiki.chiu.micro.user.convertor.MenuAuthorityVoConvertor;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;
import wiki.chiu.micro.user.wrapper.MenuAuthorityWrapper;

@Service
public class MenuAuthorityServiceImpl implements MenuAuthorityService {

  private final MenuAuthorityWrapper menuAuthorityWrapper;

  private final RoleRepository roleRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final MenuAuthorityRepository menuAuthorityRepository;

  private final AuthorityRepository authorityRepository;

  public MenuAuthorityServiceImpl(
      MenuAuthorityWrapper menuAuthorityWrapper,
      RoleRepository roleRepository,
      ApplicationEventPublisher eventPublisher,
      MenuAuthorityRepository menuAuthorityRepository,
      AuthorityRepository authorityRepository) {
    this.menuAuthorityWrapper = menuAuthorityWrapper;
    this.roleRepository = roleRepository;
    this.eventPublisher = eventPublisher;
    this.menuAuthorityRepository = menuAuthorityRepository;
    this.authorityRepository = authorityRepository;
  }

  @Override
  @Transactional
  public void saveAuthority(Long menuId, List<Long> authorityIds) {
    List<MenuAuthorityEntity> roleAuthorityEntities =
        MenuAuthorityEntityConvertor.convert(menuId, authorityIds);
    menuAuthorityWrapper.saveAuthority(menuId, roleAuthorityEntities);
    // 删除权限缓存
    executeDelMenuAuthTask();
  }

  private void executeDelMenuAuthTask() {
    List<String> allRoleCodes = roleRepository.findAllCodes();
    var authMenuIndexMessage =
        new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.AUTH.getType());
    eventPublisher.publishEvent(new AuthMenuOperateEvent(authMenuIndexMessage));
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
