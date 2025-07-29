package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;

import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.lang.TypeEnum;
import wiki.chiu.micro.common.vo.ButtonRpcVo;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.common.vo.MenusAndButtonsRpcVo;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.ButtonRpcVoConvertor;
import wiki.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import wiki.chiu.micro.user.convertor.MenuRpcVoConvertor;
import wiki.chiu.micro.user.convertor.RoleMenuEntityConvertor;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.RoleMenuService;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;
import wiki.chiu.micro.user.wrapper.RoleMenuWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.TypeEnum.*;

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

    private final ExecutorService taskExecutor;

    public RoleMenuServiceImpl(MenuRepository menuRepository, RoleMenuRepository roleMenuRepository, RoleMenuWrapper roleMenuWrapper, RoleRepository roleRepository, ApplicationContext applicationContext, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleMenuWrapper = roleMenuWrapper;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
    }

    private List<RoleMenuVo> setCheckMenusInfo(List<MenuDisplayVo> menusInfo, List<Long> menuIdsByRole, List<RoleMenuVo> parentChildren) {
        menusInfo.forEach(item -> {
            RoleMenuVo.RoleMenuVoBuilder builder = RoleMenuVo.builder()
                    .title(item.title())
                    .menuId(item.id());

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
        List<MenuDisplayVo> menusInfo =  MenuDisplayVoConvertor.buildTreeMenu(menuEntities);

        List<Long> menuIdsByRole = roleMenuRepository.findMenuIdsByRoleId(roleId);
        return setCheckMenusInfo(menusInfo, menuIdsByRole, new ArrayList<>());
    }

    @Override
    public void saveMenu(Long roleId, List<Long> menuIds) {
        List<RoleMenuEntity> roleMenuEntities = RoleMenuEntityConvertor.convert(roleId, menuIds);

        roleMenuWrapper.saveMenu(roleId, new ArrayList<>(roleMenuEntities));
        // 按钮
        taskExecutor.execute(() -> roleRepository.findById(roleId)
                .map(RoleEntity::getCode)
                .ifPresent(role -> {
                    var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(role), AuthMenuOperateEnum.AUTH_AND_MENU.getType());
                    applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
                }));
    }

    @Override
    public MenusAndButtonsRpcVo getCurrentRoleNav(String role) {
        Optional<RoleEntity> roleEntity = roleRepository.findByCode(role);

        if (roleEntity.isEmpty() || StatusEnum.HIDE.getCode().equals(roleEntity.get().getStatus())) {
            return MenusAndButtonsRpcVo.builder()
                    .menus(Collections.emptyList())
                    .buttons(Collections.emptyList())
                    .build();
        }

        List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleId(roleEntity.get().getId());
        List<MenuEntity> allKindsInfo = menuRepository.findAllById(menuIds);

        List<MenuEntity> menus = filterMenuEntities(allKindsInfo, CATALOGUE, MENU);
        List<MenuEntity> buttons = filterMenuEntities(allKindsInfo, BUTTON);

        List<MenuRpcVo> menuDtos = MenuRpcVoConvertor.convert(menus);
        List<ButtonRpcVo> buttonDtos = ButtonRpcVoConvertor.convert(buttons);

        return MenusAndButtonsRpcVo.builder()
                .buttons(buttonDtos)
                .menus(menuDtos)
                .build();
    }

    private List<MenuEntity> filterMenuEntities(List<MenuEntity> entities, TypeEnum... types) {
        List<Integer> typeCodes = Arrays.stream(types)
                .map(TypeEnum::getCode)
                .toList();
        return entities.stream()
                .filter(entity -> typeCodes.contains(entity.getType()))
                .toList();
    }

}
