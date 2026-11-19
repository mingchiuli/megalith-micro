package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.user.adapter.out.persistence.convertor.UserPersistenceConvertor;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.domain.RoleDataPermission;

@Component
public class JpaRoleDataPermissionReader implements RoleDataPermissionReader {

    private final RoleDataPermissionRepository permissions;

    public JpaRoleDataPermissionReader(RoleDataPermissionRepository permissions) {
        this.permissions = permissions;
    }

    @Override
    public List<RoleDataPermission> findAll() {
        return UserPersistenceConvertor.toRoleDataPermissions(permissions.findAll());
    }

    @Override
    public List<RoleDataPermission> findByRoleId(Long roleId) {
        return UserPersistenceConvertor.toRoleDataPermissions(permissions.findByRoleId(roleId));
    }

    @Override
    public List<RoleDataPermission> findByRoleIdIn(List<Long> roleIds) {
        return UserPersistenceConvertor.toRoleDataPermissions(permissions.findByRoleIdIn(roleIds));
    }
}
