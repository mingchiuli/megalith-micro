package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.mapping.UserPersistenceMapper;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionWriter;
import wiki.chiu.micro.user.domain.RoleDataPermission;

@Component
public class RoleDataPermissionWrapper implements RoleDataPermissionWriter {

    private final RoleDataPermissionRepository roleDataPermissions;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public RoleDataPermissionWrapper(
        RoleDataPermissionRepository roleDataPermissions, AuthCacheEvictionOutbox cacheEvictions) {
        this.roleDataPermissions = roleDataPermissions;
        this.cacheEvictions = cacheEvictions;
    }

    @Transactional
    @Override
    public void saveDataPermissions(Long roleId, List<RoleDataPermission> dataPermissions) {
        roleDataPermissions.deleteByRoleId(roleId);
        roleDataPermissions.saveAll(
            dataPermissions.stream()
                .map(permission -> UserPersistenceMapper.toEntity(permission.onRole(roleId)))
                .toList());
        cacheEvictions.enqueue(List.of(), List.of(roleId), List.of(), false, false);
    }
}
