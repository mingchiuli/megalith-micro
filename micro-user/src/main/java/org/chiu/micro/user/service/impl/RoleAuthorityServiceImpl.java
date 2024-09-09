package org.chiu.micro.user.service.impl;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.RoleAuthorityEntityConvertor;
import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.entity.RoleAuthorityEntity;
import org.chiu.micro.user.entity.RoleEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.lang.AuthMenuOperateEnum;
import org.chiu.micro.user.lang.Const;
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

import static org.chiu.micro.user.lang.StatusEnum.NORMAL;

@Service
@RequiredArgsConstructor
public class RoleAuthorityServiceImpl implements RoleAuthorityService {

    private final RoleAuthorityWrapper roleAuthorityWrapper;

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    @Override
    public List<String> getAuthoritiesByRoleCodes(String roleCode) {
        
        Optional<RoleEntity> roleEntityOptional = roleRepository.findByCodeAndStatus(roleCode, NORMAL.getCode());

        if (roleEntityOptional.isEmpty()) {
            return Collections.emptyList();
        }

        RoleEntity roleEntity = roleEntityOptional.get();

        List<Long> authorityIds = roleAuthorityRepository.findByRoleId(roleEntity.getId()).stream()
                .map(RoleAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAllById(authorityIds).stream()
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .map(AuthorityEntity::getCode)
                .toList();
    }

    /**
     * @param roleId
     * @param authorityIds
     */
    @Override
    public void saveAuthority(Long roleId, List<Long> authorityIds) {
        List<RoleAuthorityEntity> roleAuthorityEntities = RoleAuthorityEntityConvertor.convert(roleId, authorityIds);
        roleAuthorityWrapper.saveAuthority(roleId, new ArrayList<>(roleAuthorityEntities));
        // 删除权限
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
        List<AuthorityEntity> allAuthorityEntities = authorityRepository.findByStatus(NORMAL.getCode());
        List<RoleAuthorityEntity> authorityEntities = roleAuthorityRepository.findByRoleId(roleId);

        List<Long> ids = authorityEntities.stream()
                .map(RoleAuthorityEntity::getAuthorityId)
                .toList();

        return allAuthorityEntities.stream()
                .filter(item -> !item.getCode().startsWith(Const.WHITELIST.getInfo()))
                .map(item -> RoleAuthorityVo.builder()
                        .authorityId(item.getId())
                        .code(item.getCode())
                        .check(ids.contains(item.getId()))
                        .build())
                .toList();
    }
}
