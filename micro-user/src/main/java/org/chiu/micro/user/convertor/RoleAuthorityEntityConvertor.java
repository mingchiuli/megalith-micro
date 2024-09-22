package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.RoleAuthorityEntity;

import java.util.List;

public class RoleAuthorityEntityConvertor {

    private RoleAuthorityEntityConvertor() {
    }

    public static List<RoleAuthorityEntity> convert(Long roleId, List<Long> authorityIds) {
        return authorityIds.stream()
                .map(authorityId -> RoleAuthorityEntity.builder()
                        .authorityId(authorityId)
                        .roleId(roleId)
                        .build())
                .toList();
    }
}
