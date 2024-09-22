package org.chiu.micro.user.wrapper;

import org.chiu.micro.user.entity.RoleAuthorityEntity;
import org.chiu.micro.user.repository.RoleAuthorityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class RoleAuthorityWrapper {

    private final RoleAuthorityRepository roleAuthorityRepository;

    public RoleAuthorityWrapper(RoleAuthorityRepository roleAuthorityRepository) {
        this.roleAuthorityRepository = roleAuthorityRepository;
    }

    @Transactional
    public void saveAuthority(Long roleId, List<RoleAuthorityEntity> roleAuthorityEntities) {
        roleAuthorityRepository.deleteByRoleId(roleId);
        roleAuthorityRepository.saveAll(roleAuthorityEntities);
    }
}
