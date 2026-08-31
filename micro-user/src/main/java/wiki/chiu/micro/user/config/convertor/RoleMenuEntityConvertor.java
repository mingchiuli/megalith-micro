package wiki.chiu.micro.user.config.convertor;

import java.util.List;

import wiki.chiu.micro.user.domain.RoleMenuEntity;

public class RoleMenuEntityConvertor {

    private RoleMenuEntityConvertor() {
    }

    public static List<RoleMenuEntity> convert(Long roleId, List<Long> menuIds) {
        return menuIds.stream()
            .map(menuId -> RoleMenuEntity.builder().menuId(menuId).roleId(roleId).build())
            .toList();
    }
}
