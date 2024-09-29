package org.chiu.micro.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import org.chiu.micro.user.convertor.MenuEntityConvertor;
import org.chiu.micro.user.convertor.MenuEntityVoConvertor;
import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.exception.MissException;
import org.chiu.micro.user.lang.AuthMenuOperateEnum;
import org.chiu.micro.user.lang.StatusEnum;
import org.chiu.micro.user.repository.MenuRepository;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.req.MenuEntityReq;
import org.chiu.micro.user.service.MenuService;
import org.chiu.micro.user.vo.MenuDisplayVo;
import org.chiu.micro.user.vo.MenuEntityVo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.chiu.micro.user.lang.ExceptionMessage.MENU_NOT_EXIST;
import static org.chiu.micro.user.lang.ExceptionMessage.NO_FOUND;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    private final ObjectMapper objectMapper;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    public MenuServiceImpl(MenuRepository menuRepository, ObjectMapper objectMapper, RoleRepository roleRepository, ApplicationContext applicationContext) {
        this.menuRepository = menuRepository;
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
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
        List<MenuDisplayVo> menuEntities = MenuDisplayVoConvertor.convert(menus, false);
        return MenuDisplayVoConvertor.buildTreeMenu(menuEntities);
    }

    @Override
    public byte[] download() {
        List<MenuEntity> menus = menuRepository.findAll();
        try {
            return objectMapper.writeValueAsBytes(menus);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
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
