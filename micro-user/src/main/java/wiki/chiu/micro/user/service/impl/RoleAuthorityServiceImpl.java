package wiki.chiu.micro.user.service.impl;

import wiki.chiu.micro.user.entity.*;
import wiki.chiu.micro.user.repository.*;
import wiki.chiu.micro.user.service.RoleAuthorityService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;


@Service
public class RoleAuthorityServiceImpl implements RoleAuthorityService {

    private final RoleMenuRepository roleMenuRepository;

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    public RoleAuthorityServiceImpl(RoleMenuRepository roleMenuRepository, MenuAuthorityRepository menuAuthorityRepository, AuthorityRepository authorityRepository, RoleRepository roleRepository) {
        this.roleMenuRepository = roleMenuRepository;
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Set<String> getAuthoritiesByRoleCodes(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .map(role -> getAuthorityCodes(role.getId()))
                .orElse(Collections.emptySet());
    }


    private Set<String> getAuthorityCodes(Long roleId) {
        List<Long> authorityIds = roleMenuRepository.findByRoleId(roleId).stream()
                .map(RoleMenuEntity::getMenuId)
                .map(menuAuthorityRepository::findByMenuId)
                .flatMap(Collection::stream)
                .map(MenuAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAllById(authorityIds).stream()
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toSet());
    }
}
