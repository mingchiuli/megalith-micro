package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.BUTTON_MUST_NOT_PARENT;
import static wiki.chiu.micro.common.lang.ExceptionMessage.CATALOGUE_CHILD_MUST_NOT_BUTTON;
import static wiki.chiu.micro.common.lang.ExceptionMessage.CATALOGUE_PARENT_MUST_PARENT;
import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_CHILDREN_MUST_BE_BUTTON;
import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_INVALID_OPERATE;
import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.lang.TypeEnum;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import wiki.chiu.micro.user.convertor.MenuEntityConvertor;
import wiki.chiu.micro.user.convertor.MenuEntityVoConvertor;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.service.MenuService;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;
import wiki.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
@Service
public class MenuServiceImpl implements MenuService {

  private static final Integer HIDE_STATUS = StatusEnum.HIDE.getCode();

  private final MenuRepository menuRepository;

  private final RoleRepository roleRepository;

  private final AuthCacheEvictionOutbox cacheEvictions;

  private final RoleMenuRepository roleMenuRepository;

  private final RoleMenuAuthorityWrapper roleMenuAuthorityWrapper;

  public MenuServiceImpl(
      MenuRepository menuRepository,
      RoleRepository roleRepository,
      AuthCacheEvictionOutbox cacheEvictions,
      RoleMenuRepository roleMenuRepository,
      RoleMenuAuthorityWrapper roleMenuAuthorityWrapper) {
    this.menuRepository = menuRepository;
    this.roleRepository = roleRepository;
    this.cacheEvictions = cacheEvictions;
    this.roleMenuRepository = roleMenuRepository;
    this.roleMenuAuthorityWrapper = roleMenuAuthorityWrapper;
  }

  @Override
  public MenuEntityVo findById(Long id) {
    MenuEntity menuEntity =
        menuRepository.findById(id).orElseThrow(() -> new MissException(MENU_NOT_EXIST.getMsg()));

    return MenuEntityVoConvertor.convert(menuEntity);
  }

  @Override
  @Transactional
  public void saveOrUpdate(MenuEntityReq menu) {
    validateMenuHierarchy(menu);
    MenuEntity dealMenu = menu.id().flatMap(menuRepository::findById).orElseGet(MenuEntity::new);
    MenuEntity menuEntity = MenuEntityConvertor.convert(menu, dealMenu);

    if (HIDE_STATUS.equals(menu.status()) && menu.id().isPresent()) {
      List<MenuEntity> menuEntities = new ArrayList<>();
      menuEntities.add(menuEntity);
      findTargetChildrenMenuId(menu.id().get(), menuEntities);
      menuRepository.saveAll(menuEntities);
    } else {
      menuRepository.save(menuEntity);
    }

    enqueueAllRoleEviction();
  }

  private void enqueueAllRoleEviction() {
    var roles = roleRepository.findAll();
    cacheEvictions.enqueue(
        List.of(),
        roles.stream().map(wiki.chiu.micro.user.entity.RoleEntity::getId).toList(),
        roles.stream().map(wiki.chiu.micro.user.entity.RoleEntity::getCode).toList(),
        true,
        false);
  }

  @Override
  public List<MenuDisplayVo> tree() {
    List<MenuEntity> menus = menuRepository.findAllByOrderByOrderNumDesc();
    List<MenuDisplayVo> menuEntities = MenuDisplayVoConvertor.convert(menus, false);
    return MenuDisplayVoConvertor.buildTreeMenu(menuEntities);
  }

  @Override
  public byte[] download() {
    List<MenuEntity> menuEntities = menuRepository.findAll();
    List<RoleMenuEntity> roleMenuEntities = roleMenuRepository.findAll();
    return SQLUtils.compose(
            SQLUtils.entityToInsertSQL(menuEntities, Const.MENU_TABLE),
            SQLUtils.entityToInsertSQL(roleMenuEntities, Const.ROLE_MENU_TABLE))
        .getBytes();
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (menuRepository.existsByParentId(id)) {
      throw new BaseException(MENU_INVALID_OPERATE);
    }
    roleMenuAuthorityWrapper.deleteMenu(id);
    enqueueAllRoleEviction();
  }

  private void findTargetChildrenMenuId(Long menuId, List<MenuEntity> menuEntities) {
    List<MenuEntity> menus = menuRepository.findByParentId(menuId);
    menus.forEach(
        menu -> {
          menu.setUpdated(LocalDateTime.now());
          menu.setStatus(StatusEnum.HIDE.getCode());
          menuEntities.add(menu);
          findTargetChildrenMenuId(menu.getId(), menuEntities);
        });
  }

  private void validateMenuHierarchy(MenuEntityReq menu) {
    TypeEnum type = TypeEnum.getInstance(menu.type());
    TypeEnum parentType = getParentType(menu.parentId());

    if (TypeEnum.BUTTON.equals(parentType)) {
      throw new BaseException(BUTTON_MUST_NOT_PARENT);
    }
    if (TypeEnum.MENU.equals(parentType) && !TypeEnum.BUTTON.equals(type)) {
      throw new BaseException(MENU_CHILDREN_MUST_BE_BUTTON);
    }
    if (TypeEnum.CATALOGUE.equals(parentType) && TypeEnum.BUTTON.equals(type)) {
      throw new BaseException(CATALOGUE_CHILD_MUST_NOT_BUTTON);
    }
    if (TypeEnum.CATALOGUE.equals(type) && !TypeEnum.CATALOGUE.equals(parentType)) {
      throw new BaseException(CATALOGUE_PARENT_MUST_PARENT);
    }
  }

  private TypeEnum getParentType(Long parentId) {
    if (Long.valueOf(0).equals(parentId)) {
      return TypeEnum.CATALOGUE;
    }
    MenuEntity parent =
        menuRepository.findById(parentId).orElseThrow(() -> new MissException(NO_FOUND));
    return TypeEnum.getInstance(parent.getType());
  }
}
