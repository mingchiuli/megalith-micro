package org.chiu.micro.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import org.chiu.micro.user.lang.AuthMenuOperateEnum;
import org.chiu.micro.user.lang.StatusEnum;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.MenuDisplayVoConvertor;
import org.chiu.micro.user.convertor.MenuEntityConvertor;
import org.chiu.micro.user.convertor.MenuEntityVoConvertor;
import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.repository.MenuRepository;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.service.MenuService;
import org.chiu.micro.user.req.MenuEntityReq;
import org.chiu.micro.user.exception.MissException;
import lombok.RequiredArgsConstructor;
import org.chiu.micro.user.vo.MenuDisplayVo;
import org.chiu.micro.user.vo.MenuEntityVo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static org.chiu.micro.user.lang.ExceptionMessage.*;
import static org.chiu.micro.user.convertor.MenuDisplayVoConvertor.buildTreeMenu;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    private final ObjectMapper objectMapper;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    @Override
    public MenuEntityVo findById(Long id) {
        MenuEntity menuEntity = menuRepository.findById(id)
                .orElseThrow(() -> new MissException(MENU_NOT_EXIST.getMsg()));

        return MenuEntityVoConvertor.convert(menuEntity);
    }

    @Override
    public void saveOrUpdate(MenuEntityReq menu) {
        Long menuId = menu.getMenuId();
        MenuEntity menuEntity;

        if (Objects.nonNull(menuId)) {
            menuEntity = menuRepository.findById(menuId)
                    .orElseThrow(() -> new MissException(NO_FOUND));
        } else {
            menuEntity = MenuEntityConvertor.convert(menu);
        }

        BeanUtils.copyProperties(menu, menuEntity);

        if (StatusEnum.HIDE.getCode().equals(menu.getStatus())) {
            List<MenuEntity> menuEntities = new ArrayList<>();
            menuEntities.add(menuEntity);
            findTargetChildrenMenuId(menuId, menuEntities);
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
        return buildTreeMenu(menuEntities);
    }

    @SneakyThrows
    @Override
    public byte[] download() {
        List<MenuEntity> menus = menuRepository.findAll();
        return objectMapper.writeValueAsBytes(menus);
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
