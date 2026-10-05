package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.enums.StatusEnum.HIDE;
import static wiki.chiu.micro.common.error.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.ArrayList;
import java.util.List;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.application.model.MenuSelection;
import wiki.chiu.micro.user.application.port.in.RoleMenuService;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuWriter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleMenu;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
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

    @Override
    public List<MenuSelection> getMenusInfo(Long roleId) {
        List<MenuNode> menusInfo = MenuTree.buildEnabled(menuRepository.findAll());
        List<Long> menuIdsByRole = roleMenuReader.findMenuIdsByRoleId(roleId);
        return toSelections(menusInfo, menuIdsByRole);
    }

    @Override
    public List<Menu> getCurrentRoleNav(String role) {
        return roleRepository
            .findByCode(role)
            .filter(item -> !HIDE.getCode().equals(item.status()))
            .map(item -> menuRepository.findAllById(roleMenuReader.findMenuIdsByRoleId(item.id())))
            .orElseGet(List::of);
    }

    @Override
    public void saveMenu(Long roleId, List<Long> menuIds) {
        Role role =
            roleRepository.findById(roleId).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
        List<RoleMenu> roleMenus =
            menuIds.stream()
                .map(menuId -> new RoleMenu(null, roleId, menuId, null, null))
                .toList();

        roleMenuWrapper.saveMenu(roleId, role.code(), new ArrayList<>(roleMenus));
    }

    private static List<MenuSelection> toSelections(
        List<MenuNode> nodes, List<Long> menuIdsByRole) {
        return nodes.stream()
            .map(
                node ->
                    new MenuSelection(
                        node.menu().id(),
                        node.menu().title(),
                        menuIdsByRole.contains(node.menu().id()),
                        toSelections(node.children(), menuIdsByRole)))
            .toList();
    }
}
