package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.AuthStatusEnum;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.RoleAuthorityEntityConvertor;
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.RoleAuthorityEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.RoleAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.RoleAuthorityService;
import wiki.chiu.micro.user.vo.RoleAuthorityVo;
import wiki.chiu.micro.user.wrapper.RoleAuthorityWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;


@Service
public class RoleAuthorityServiceImpl implements RoleAuthorityService {

    private final RoleAuthorityWrapper roleAuthorityWrapper;

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    private final ExecutorService taskExecutor;

    public RoleAuthorityServiceImpl(RoleAuthorityWrapper roleAuthorityWrapper, RoleAuthorityRepository roleAuthorityRepository, AuthorityRepository authorityRepository, RoleRepository roleRepository, ApplicationContext applicationContext, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.roleAuthorityWrapper = roleAuthorityWrapper;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Set<String> getAuthoritiesByRoleCodes(String roleCode) {
        return roleRepository.findByCodeAndStatus(roleCode, NORMAL.getCode())
                .map(role -> getAuthorityCodes(role.getId()))
                .orElse(Collections.emptySet());
    }


    private Set<String> getAuthorityCodes(Long roleId) {
        List<Long> authorityIds = roleAuthorityRepository.findByRoleId(roleId).stream()
                .map(RoleAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAllById(authorityIds).stream()
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    public void saveAuthority(Long roleId, List<Long> authorityIds) {
        List<RoleAuthorityEntity> roleAuthorityEntities = RoleAuthorityEntityConvertor.convert(roleId, authorityIds);
        roleAuthorityWrapper.saveAuthority(roleId, new ArrayList<>(roleAuthorityEntities));
        // 删除权限缓存
        executeDelRoleAuthTask(roleId, AuthMenuOperateEnum.AUTH.getType());
    }

    private void executeDelRoleAuthTask(Long roleId, Integer type) {
        taskExecutor.execute(() -> roleRepository.findById(roleId)
                .map(RoleEntity::getCode)
                .ifPresent(role -> {
                    var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(role), type);
                    applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
                }));
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
