package wiki.chiu.micro.user.service.impl;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import wiki.chiu.micro.user.convertor.MenuRpcVoConvertor;
import wiki.chiu.micro.user.convertor.RoleMenuEntityConvertor;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;
import wiki.chiu.micro.user.wrapper.RoleMenuWrapper;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

  private final MenuRepository menuRepository;

  private final RoleMenuRepository roleMenuRepository;

  private final RoleMenuWrapper roleMenuWrapper;

  private final RoleRepository roleRepository;

  private final AuthCacheEvictionOutbox cacheEvictions;

  public RoleMenuServiceImpl(
      MenuRepository menuRepository,
      RoleMenuRepository roleMenuRepository,
      RoleMenuWrapper roleMenuWrapper,
      RoleRepository roleRepository,
      AuthCacheEvictionOutbox cacheEvictions) {
    this.menuRepository = menuRepository;
    this.roleMenuRepository = roleMenuRepository;
    this.roleMenuWrapper = roleMenuWrapper;
    this.roleRepository = roleRepository;
    this.cacheEvictions = cacheEvictions;
  }

  private List<RoleMenuVo> setCheckMenusInfo(
      List<MenuDisplayVo> menusInfo, List<Long> menuIdsByRole, List<RoleMenuVo> parentChildren) {
    menusInfo.forEach(
        item -> {
          RoleMenuVo.RoleMenuVoBuilder builder =
              RoleMenuVo.builder().title(item.title()).menuId(item.id());

          if (menuIdsByRole.contains(item.id())) {
            builder.check(true);
          }

          if (!item.children().isEmpty()) {
            List<RoleMenuVo> children = new ArrayList<>();
            builder.children(children);
            setCheckMenusInfo(item.children(), menuIdsByRole, children);
          }
          parentChildren.add(builder.build());
        });

    return parentChildren;
  }

  public List<RoleMenuVo> getMenusInfo(Long roleId) {
    List<Long> menuIds = menuRepository.findAllIds();
    List<MenuEntity> menus = menuRepository.findAllById(menuIds);
    List<MenuDisplayVo> menuEntities = MenuDisplayVoConvertor.convert(menus, true);
    // 转树状结构
    List<MenuDisplayVo> menusInfo = MenuDisplayVoConvertor.buildTreeMenu(menuEntities);

    List<Long> menuIdsByRole = roleMenuRepository.findMenuIdsByRoleId(roleId);
    return setCheckMenusInfo(menusInfo, menuIdsByRole, new ArrayList<>());
  }

  @Override
  @Transactional
  public void saveMenu(Long roleId, List<Long> menuIds) {
    List<RoleMenuEntity> roleMenuEntities = RoleMenuEntityConvertor.convert(roleId, menuIds);

    roleMenuWrapper.saveMenu(roleId, new ArrayList<>(roleMenuEntities));
    roleRepository
        .findById(roleId)
        .map(RoleEntity::getCode)
        .ifPresent(
            role -> {
              cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(role), true, true, false);
            });
  }

  @Override
  public List<MenuRpcVo> getCurrentRoleNav(String role) {
    Optional<RoleEntity> roleEntity = roleRepository.findByCode(role);

    if (roleEntity.isEmpty() || StatusEnum.HIDE.getCode().equals(roleEntity.get().getStatus())) {
      return Collections.emptyList();
    }

    List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleId(roleEntity.get().getId());
    List<MenuEntity> allKindsInfo = menuRepository.findAllById(menuIds);

    return MenuRpcVoConvertor.convert(allKindsInfo);
  }
}
