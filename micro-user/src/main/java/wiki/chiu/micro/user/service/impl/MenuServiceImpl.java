package wiki.chiu.micro.user.service.impl;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import wiki.chiu.micro.user.convertor.MenuEntityConvertor;
import wiki.chiu.micro.user.convertor.MenuEntityVoConvertor;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.service.MenuService;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_NOT_EXIST;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;


/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    private final RoleMenuRepository roleMenuRepository;

    public MenuServiceImpl(MenuRepository menuRepository, RoleRepository roleRepository, ApplicationContext applicationContext, RoleMenuRepository roleMenuRepository) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.roleMenuRepository = roleMenuRepository;
    }

    @Override
    public MenuEntityVo findById(Long id) {
        MenuEntity menuEntity = menuRepository.findById(id)
                .orElseThrow(() -> new MissException(MENU_NOT_EXIST.getMsg()));

        return MenuEntityVoConvertor.convert(menuEntity);
    }

    @Override
    public void saveOrUpdate(MenuEntityReq menu) {
        Optional<Long> menuId = menu.menuId();
        MenuEntity menuEntity;

        if (menuId.isPresent()) {
            menuEntity = menuRepository.findById(menuId.get())
                    .orElseThrow(() -> new MissException(NO_FOUND));
        } else {
            menuEntity = MenuEntityConvertor.convert(menu);
        }

        MenuEntityConvertor.convert(menu, menuEntity);

        if (StatusEnum.HIDE.getCode().equals(menu.status()) && menuId.isPresent()) {
            List<MenuEntity> menuEntities = new ArrayList<>();
            menuEntities.add(menuEntity);
            findTargetChildrenMenuId(menuId.get(), menuEntities);
            menuRepository.saveAll(menuEntities);
        } else {
            menuRepository.save(menuEntity);
        }

        // 全部按钮和菜单
        List<String> allRoleCodes = roleRepository.findAllCodes();
        var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.MENU.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
    }

    @Override
    public List<MenuDisplayVo> tree() {
        List<MenuEntity> menus = menuRepository.findAllByOrderByOrderNumDesc();
        List<MenuDisplayVo> menuEntities = MenuDisplayVoConvertor.convert(menus);
        return MenuDisplayVoConvertor.buildTreeMenu(menuEntities);
    }

    @Override
    public byte[] download() {
        List<MenuEntity> menuEntities = menuRepository.findAll();
        List<RoleMenuEntity> roleMenuEntities = roleMenuRepository.findAll();
        String menus = SQLUtils.entityToInsertSQL(menuEntities, Const.MENU_TABLE);
        String roleMenus = SQLUtils.entityToInsertSQL(roleMenuEntities, Const.ROLE_MENU_TABLE);

        return (menus + "\n" + roleMenus).getBytes();
    }

    private void findTargetChildrenMenuId(Long menuId, List<MenuEntity> menuEntities) {
        List<MenuEntity> menus = menuRepository.findByParentId(menuId);
        menus.forEach(menu -> {
            menu.setUpdated(LocalDateTime.now());
            menu.setStatus(StatusEnum.HIDE.getCode());
            menuEntities.add(menu);
            findTargetChildrenMenuId(menu.getMenuId(), menuEntities);
        });
    }
}
