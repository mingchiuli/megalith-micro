package org.chiu.micro.user.service.impl;

import org.chiu.micro.common.exception.CommitException;
import org.chiu.micro.common.lang.AuthMenuOperateEnum;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.*;
import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.entity.RoleMenuEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.repository.MenuRepository;
import org.chiu.micro.user.repository.RoleMenuRepository;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.service.RoleMenuService;
import org.chiu.micro.user.vo.*;
import org.chiu.micro.user.wrapper.RoleMenuWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.chiu.micro.common.lang.ExceptionMessage.MENU_INVALID_OPERATE;
import static org.chiu.micro.common.lang.StatusEnum.NORMAL;
import static org.chiu.micro.common.lang.TypeEnum.*;

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

    private final ApplicationContext applicationContext;


    public RoleMenuServiceImpl(MenuRepository menuRepository, RoleMenuRepository roleMenuRepository, RoleMenuWrapper roleMenuWrapper, RoleRepository roleRepository, ApplicationContext applicationContext) {
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleMenuWrapper = roleMenuWrapper;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
    }

    private List<RoleMenuVo> setCheckMenusInfo(List<MenuDisplayVo> menusInfo, List<Long> menuIdsByRole, List<RoleMenuVo> parentChildren) {
        menusInfo.forEach(item -> {
            RoleMenuVo.RoleMenuVoBuilder builder = RoleMenuVo.builder()
                    .title(item.title())
                    .menuId(item.menuId());

            if (menuIdsByRole.contains(item.menuId())) {
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
        List<MenuDisplayVo> menuEntities = MenuDisplayVoConvertor.convert(menus);
        // 转树状结构
        List<MenuDisplayVo> menusInfo =  MenuDisplayVoConvertor.buildTreeMenu(menuEntities);

        List<Long> menuIdsByRole = roleMenuRepository.findMenuIdsByRoleId(roleId);
        return setCheckMenusInfo(menusInfo, menuIdsByRole, new ArrayList<>());
    }

    @Override
    public void saveMenu(Long roleId, List<Long> menuIds) {
        List<RoleMenuEntity> roleMenuEntities = RoleMenuEntityConvertor.convert(roleId, menuIds);

        roleMenuWrapper.saveMenu(roleId, new ArrayList<>(roleMenuEntities));
        // 按钮
        roleRepository.findById(roleId)
                .map(RoleEntity::getCode)
                .ifPresent(role -> {
                    var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(role), AuthMenuOperateEnum.MENU.getType());
                    applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
                });
    }

    @Override
    public void delete(Long id) {
        List<MenuEntity> menus = menuRepository.findByParentId(id);
        if (Boolean.FALSE.equals(menus.isEmpty())) {
            throw new CommitException(MENU_INVALID_OPERATE);
        }
        roleMenuWrapper.deleteMenu(id);
        //全部按钮
        List<String> allRoleCodes = roleRepository.findAllCodes();
        var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.MENU.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
    }

    @Override
    public MenusAndButtonsRpcVo getCurrentRoleNav(String role) {

        Optional<RoleEntity> roleEntity = roleRepository.findByCodeAndStatus(role, NORMAL.getCode());

        if (roleEntity.isEmpty()) {
            return MenusAndButtonsRpcVo.builder()
                    .menus(Collections.emptyList())
                    .buttons(Collections.emptyList())
                    .build();
        }

        List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleId(roleEntity.get().getId());

        List<MenuEntity> allKindsInfo = menuRepository.findAllById(menuIds);

        List<MenuEntity> menus = allKindsInfo
                .stream()
                .filter(menu -> CATALOGUE.getCode().equals(menu.getType()) || MENU.getCode().equals(menu.getType()))
                .toList();

        List<MenuEntity> buttons = allKindsInfo
                .stream()
                .filter(menu -> BUTTON.getCode().equals(menu.getType()))
                .toList();

        List<MenuVo> menuDtos = MenuVoConvertor.convert(menus);
        List<ButtonVo> buttonDtos = ButtonVoConvertor.convert(buttons);

        return MenusAndButtonsRpcVo.builder()
                .buttons(buttonDtos)
                .menus(menuDtos)
                .build();
    }
}
