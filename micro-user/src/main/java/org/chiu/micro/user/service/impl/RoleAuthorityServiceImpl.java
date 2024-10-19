package org.chiu.micro.user.service.impl;

import org.chiu.micro.common.lang.AuthMenuOperateEnum;
import org.chiu.micro.common.lang.AuthStatusEnum;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.RoleAuthorityEntityConvertor;
import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.entity.RoleAuthorityEntity;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.repository.AuthorityRepository;
import org.chiu.micro.user.repository.RoleAuthorityRepository;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.service.RoleAuthorityService;
import org.chiu.micro.user.vo.RoleAuthorityVo;
import org.chiu.micro.user.wrapper.RoleAuthorityWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.chiu.micro.common.lang.StatusEnum.NORMAL;


@Service
public class RoleAuthorityServiceImpl implements RoleAuthorityService {

    private final RoleAuthorityWrapper roleAuthorityWrapper;

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;


    public RoleAuthorityServiceImpl(RoleAuthorityWrapper roleAuthorityWrapper, RoleAuthorityRepository roleAuthorityRepository, AuthorityRepository authorityRepository, RoleRepository roleRepository, ApplicationContext applicationContext) {
        this.roleAuthorityWrapper = roleAuthorityWrapper;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public List<String> getAuthoritiesByRoleCodes(String roleCode) {

        Optional<RoleEntity> roleEntity = roleRepository.findByCodeAndStatus(roleCode, NORMAL.getCode());

        if (roleEntity.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> authorityIds = roleAuthorityRepository.findByRoleId(roleEntity.get().getId()).stream()
                .map(RoleAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAllById(authorityIds).stream()
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .map(AuthorityEntity::getCode)
                .toList();
    }

    @Override
    public void saveAuthority(Long roleId, List<Long> authorityIds) {
        List<RoleAuthorityEntity> roleAuthorityEntities = RoleAuthorityEntityConvertor.convert(roleId, authorityIds);
        roleAuthorityWrapper.saveAuthority(roleId, new ArrayList<>(roleAuthorityEntities));
        // 删除权限缓存
        roleRepository.findById(roleId)
                .map(RoleEntity::getCode)
                .ifPresent(role -> {
                    var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(role),
                            AuthMenuOperateEnum.AUTH.getType());
                    applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
                });
    }

    @Override
    public List<RoleAuthorityVo> getAuthoritiesInfo(Long roleId) {
        List<Long> ids = roleAuthorityRepository.findByRoleId(roleId).stream()
                .map(RoleAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findByStatus(NORMAL.getCode()).stream()
                .filter(item -> AuthStatusEnum.NEED_AUTH.getCode().equals(item.getType()))
                .map(item -> RoleAuthorityVo.builder()
                        .authorityId(item.getId())
                        .code(item.getCode())
                        .check(ids.contains(item.getId()))
                        .build())
                .toList();
    }
}
