package org.chiu.micro.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.RoleEntityRpcVoConvertor;
import org.chiu.micro.user.convertor.RoleEntityVoConvertor;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.service.RoleService;
import org.chiu.micro.user.req.RoleEntityReq;
import org.chiu.micro.user.exception.MissException;
import org.chiu.micro.user.lang.AuthMenuOperateEnum;
import org.chiu.micro.user.lang.StatusEnum;
import org.chiu.micro.user.page.PageAdapter;
import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.vo.RoleEntityRpcVo;
import org.chiu.micro.user.vo.RoleEntityVo;
import org.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Collections;

import static org.chiu.micro.user.lang.ExceptionMessage.ROLE_NOT_EXIST;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final ObjectMapper objectMapper;

    private final RoleMenuAuthorityWrapper roleMenuAuthorityWrapper;

    private final ApplicationContext applicationContext;

    @Override
    public RoleEntityVo info(Long id) {
        RoleEntity roleEntity = roleRepository.findById(id)
                .orElseThrow(() -> new MissException(ROLE_NOT_EXIST));

        return RoleEntityVoConvertor.convert(roleEntity);
    }

    @Override
    public PageAdapter<RoleEntityVo> getPage(Integer currentPage, Integer size) {
        var pageRequest = PageRequest.of(currentPage - 1,
                size,
                Sort.by("created").ascending());
        Page<RoleEntity> page = roleRepository.findAll(pageRequest);

        return RoleEntityVoConvertor.convert(page);
    }

    @Override
    public void saveOrUpdate(RoleEntityReq roleReq) {

        Long id = roleReq.getId();
        RoleEntity roleEntity;

        if (Objects.nonNull(id)) {
            roleEntity = roleRepository.findById(id)
                    .orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
        } else {
            roleEntity = new RoleEntity();
        }

        BeanUtils.copyProperties(roleReq, roleEntity);
        roleRepository.save(roleEntity);
        //权限和按钮
        var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(roleEntity.getCode()), AuthMenuOperateEnum.AUTH_AND_MENU.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
    }

    @Override
    public void delete(List<Long> ids) {
        roleMenuAuthorityWrapper.delete(ids);
        //多个角色删除
        var roles = roleRepository.findAllById(ids)
                .stream()
                .map(RoleEntity::getCode)
                .distinct()
                .toList();

        var authMenuIndexMessage = new AuthMenuIndexMessage(roles, AuthMenuOperateEnum.AUTH_AND_MENU.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));        
    }

    @SneakyThrows
    @Override
    public byte[] download() {
        List<RoleEntity> roles = roleRepository.findAll();
        return objectMapper.writeValueAsBytes(roles);
    }

    @Override
    public List<RoleEntityVo> getValidAll() {
        List<RoleEntity> entities = roleRepository.findByStatus(StatusEnum.NORMAL.getCode());
        return RoleEntityVoConvertor.convert(entities);
    }

    @Override
    public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        List<RoleEntity> entities = roleRepository.findByCodeInAndStatus(roles, status);
        return RoleEntityRpcVoConvertor.convert(entities);
    }
}
