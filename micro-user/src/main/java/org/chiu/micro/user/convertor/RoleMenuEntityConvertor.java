package org.chiu.micro.user.convertor;

import org.chiu.micro.user.entity.RoleMenuEntity;

import java.util.List;

public class RoleMenuEntityConvertor {

    private RoleMenuEntityConvertor() {}

    public static List<RoleMenuEntity> convert(Long roleId, List<Long> menuIds) {
        return menuIds.stream()
                .map(menuId -> RoleMenuEntity.builder()
                        .menuId(menuId)
                        .roleId(roleId)
                        .build())
                .toList();
    }
}
