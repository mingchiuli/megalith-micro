package org.chiu.micro.user.wrapper;

import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.entity.RoleAuthorityEntity;
import org.chiu.micro.user.repository.RoleAuthorityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
public class RoleAuthorityWrapper {

    private final RoleAuthorityRepository roleAuthorityRepository;

    @Transactional
    public void saveAuthority(Long roleId, List<RoleAuthorityEntity> roleAuthorityEntities) {
        roleAuthorityRepository.deleteByRoleId(roleId);
        roleAuthorityRepository.flush();
        roleAuthorityRepository.saveAll(roleAuthorityEntities);
    }
}
