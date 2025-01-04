package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.ExceptionMessage.MENU_NOT_EXIST;


/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final Integer HIDE_STATUS = StatusEnum.HIDE.getCode();

    private final MenuRepository menuRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    private final RoleMenuRepository roleMenuRepository;

    private final ExecutorService taskExecutor;

    public MenuServiceImpl(MenuRepository menuRepository, RoleRepository roleRepository, ApplicationContext applicationContext, RoleMenuRepository roleMenuRepository, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.roleMenuRepository = roleMenuRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public MenuEntityVo findById(Long id) {
        MenuEntity menuEntity = menuRepository.findById(id)
                .orElseThrow(() -> new MissException(MENU_NOT_EXIST.getMsg()));

        return MenuEntityVoConvertor.convert(menuEntity);
    }


    @Override
    public void saveOrUpdate(MenuEntityReq menu) {
        MenuEntity dealMenu = menu.menuId()
                .flatMap(menuRepository::findById)
                .orElseGet(MenuEntity::new);
        MenuEntity menuEntity = MenuEntityConvertor.convert(menu, dealMenu);

        if (HIDE_STATUS.equals(menu.status()) && menu.menuId().isPresent()) {
            List<MenuEntity> menuEntities = new ArrayList<>();
            menuEntities.add(menuEntity);
            findTargetChildrenMenuId(menu.menuId().get(), menuEntities);
            menuRepository.saveAll(menuEntities);
        } else {
            menuRepository.save(menuEntity);
        }

        executeDelAllRoleMenuTask(AuthMenuOperateEnum.MENU.getType());
    }

    private void executeDelAllRoleMenuTask(Integer type) {
        taskExecutor.execute(() -> {
            List<String> allRoleCodes = roleRepository.findAllCodes();
            var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, type);
            applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
        });
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
        return SQLUtils.compose(
                SQLUtils.entityToInsertSQL(menuEntities, Const.MENU_TABLE),
                SQLUtils.entityToInsertSQL(roleMenuEntities, Const.ROLE_MENU_TABLE))
                .getBytes();
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
