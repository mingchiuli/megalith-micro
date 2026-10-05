package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.error.ExceptionMessage.BUTTON_MUST_NOT_PARENT;
import static wiki.chiu.micro.common.error.ExceptionMessage.CATALOGUE_CHILD_MUST_NOT_BUTTON;
import static wiki.chiu.micro.common.error.ExceptionMessage.CATALOGUE_PARENT_MUST_PARENT;
import static wiki.chiu.micro.common.error.ExceptionMessage.MENU_CHILDREN_MUST_BE_BUTTON;
import static wiki.chiu.micro.common.error.ExceptionMessage.MENU_INVALID_OPERATE;
import static wiki.chiu.micro.common.error.ExceptionMessage.MENU_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.enums.TypeEnum;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.MenuDraft;
import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.application.model.SqlTables;
import wiki.chiu.micro.user.application.port.in.MenuService;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.application.port.out.MenuWriter;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.Menu;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.RoleMenu;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
public class MenuServiceImpl implements MenuService {

    private static final Integer HIDE_STATUS = StatusEnum.HIDE.getCode();

    private final MenuReader menuRepository;

    private final RoleMenuReader roleMenuReader;

    private final MenuWriter roleMenuAuthorityWrapper;
    private final RoleReader roleRepository;

    public MenuServiceImpl(
        MenuReader menuRepository,
        RoleMenuReader roleMenuReader,
        MenuWriter roleMenuAuthorityWrapper,
        RoleReader roleRepository) {
        this.menuRepository = menuRepository;
        this.roleMenuReader = roleMenuReader;
        this.roleMenuAuthorityWrapper = roleMenuAuthorityWrapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Menu> findAll() {
        return menuRepository.findAll();
    }

    @Override
    public Menu findById(Long id) {
        return menuRepository.findById(id).orElseThrow(() -> new MissException(MENU_NOT_EXIST.getMsg()));
    }

    @Override
    public List<MenuNode> tree() {
        return MenuTree.build(menuRepository.findAllByOrderByOrderNumDesc());
    }

    @Override
    public void saveOrUpdate(MenuDraft menu) {
        validateMenuHierarchy(menu);
        Menu dealMenu =
            menu.id() == null
                ? Menu.blank()
                : menuRepository.findById(menu.id()).orElseGet(Menu::blank);
        Menu menuEntity = menu.mergeInto(dealMenu);
        List<Role> roles = roleRepository.findAll();

        if (HIDE_STATUS.equals(menu.status()) && menu.id() != null) {
            List<Menu> menuEntities = new ArrayList<>();
            menuEntities.add(menuEntity);
            findTargetChildrenMenuId(menu.id(), menuEntities);
            roleMenuAuthorityWrapper.saveMenus(menuEntities, roleIds(roles), roleCodes(roles));
        } else {
            roleMenuAuthorityWrapper.saveMenus(List.of(menuEntity), roleIds(roles), roleCodes(roles));
        }
    }

    @Override
    public byte[] download() {
        List<Menu> menuEntities = menuRepository.findAll();
        List<RoleMenu> roleMenuEntities = roleMenuReader.findAll();
        return SQLUtils.compose(
                SQLUtils.insertSql(menuEntities, SqlTables.MENU),
                SQLUtils.insertSql(roleMenuEntities, SqlTables.ROLE_MENU))
            .getBytes();
    }

    @Override
    public void delete(Long id) {
        if (menuRepository.existsByParentId(id)) {
            throw new BaseException(MENU_INVALID_OPERATE);
        }
        List<Role> roles = roleRepository.findAll();
        roleMenuAuthorityWrapper.deleteMenu(id, roleIds(roles), roleCodes(roles));
    }

    private List<Long> roleIds(List<Role> roles) {
        return roles.stream().map(Role::id).toList();
    }

    private List<String> roleCodes(List<Role> roles) {
        return roles.stream().map(Role::code).toList();
    }

    private void findTargetChildrenMenuId(Long menuId, List<Menu> menuEntities) {
        List<Menu> menus = menuRepository.findByParentId(menuId);
        menus.forEach(
            menu -> {
                Menu hidden =
                    menu.withUpdated(LocalDateTime.now()).withStatus(StatusEnum.HIDE.getCode());
                menuEntities.add(hidden);
                findTargetChildrenMenuId(hidden.id(), menuEntities);
            });
    }

    private void validateMenuHierarchy(MenuDraft menu) {
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
        Menu parent =
            menuRepository.findById(parentId).orElseThrow(() -> new MissException(NO_FOUND));
        return TypeEnum.getInstance(parent.type());
    }
}
