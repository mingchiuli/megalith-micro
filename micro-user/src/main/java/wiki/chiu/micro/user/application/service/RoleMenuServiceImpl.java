package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.*;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.application.port.in.RoleMenuService;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuWriter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.config.convertor.MenuDisplayVoConvertor;
import wiki.chiu.micro.user.config.convertor.MenuRpcVoConvertor;
import wiki.chiu.micro.user.config.convertor.RoleMenuEntityConvertor;
import wiki.chiu.micro.user.domain.MenuEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    private final MenuReader menuRepository;

    private final RoleMenuReader roleMenuReader;

    private final RoleMenuWriter roleMenuWrapper;

    private final RoleReader roleRepository;

    public RoleMenuServiceImpl(
        MenuReader menuRepository,
        RoleMenuReader roleMenuReader,
        RoleMenuWriter roleMenuWrapper,
        RoleReader roleRepository) {
        this.menuRepository = menuRepository;
        this.roleMenuReader = roleMenuReader;
        this.roleMenuWrapper = roleMenuWrapper;
        this.roleRepository = roleRepository;
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

        List<Long> menuIdsByRole = roleMenuReader.findMenuIdsByRoleId(roleId);
        return setCheckMenusInfo(menusInfo, menuIdsByRole, new ArrayList<>());
    }

    @Override
    public void saveMenu(Long roleId, List<Long> menuIds) {
        RoleEntity role =
            roleRepository.findById(roleId).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
        List<RoleMenuEntity> roleMenuEntities = RoleMenuEntityConvertor.convert(roleId, menuIds);

        roleMenuWrapper.saveMenu(roleId, role.getCode(), new ArrayList<>(roleMenuEntities));
    }

    @Override
    public List<MenuRpcVo> getCurrentRoleNav(String role) {
        Optional<RoleEntity> roleEntity = roleRepository.findByCode(role);

        if (roleEntity.isEmpty() || StatusEnum.HIDE.getCode().equals(roleEntity.get().getStatus())) {
            return Collections.emptyList();
        }

        List<Long> menuIds = roleMenuReader.findMenuIdsByRoleId(roleEntity.get().getId());
        List<MenuEntity> allKindsInfo = menuRepository.findAllById(menuIds);

        return MenuRpcVoConvertor.convert(allKindsInfo);
    }
}
