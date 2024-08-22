package org.chiu.micro.user.repository;

import org.chiu.micro.user.entity.RoleAuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleAuthorityRepository extends JpaRepository<RoleAuthorityEntity, Long> {

    void deleteByRoleId(Long roleId);

    List<RoleAuthorityEntity> findByRoleId(Long id);

    List<RoleAuthorityEntity> findByRoleIdIn(List<Long> ids);

    @Query(value = "DELETE from RoleAuthorityEntity roleAuthority where roleAuthority.roleId in (?1)")
    @Modifying
    @Transactional
    void deleteAllByRoleId(List<Long> ids);
}
