package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.application.port.out.RoleWriter;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

@Component
public class RoleWrapper implements RoleWriter {

    private final RoleRepository roles;
    private final RoleMenuRepository roleMenus;
    private final UserRoleRepository userRoles;
    private final RoleDataPermissionRepository dataPermissions;
    private final AuthCacheEvictionOutbox cacheEvictions;

    public RoleWrapper(
        RoleRepository roles,
        RoleMenuRepository roleMenus,
        UserRoleRepository userRoles,
        RoleDataPermissionRepository dataPermissions,
        AuthCacheEvictionOutbox cacheEvictions) {
        this.roles = roles;
        this.roleMenus = roleMenus;
        this.userRoles = userRoles;
        this.dataPermissions = dataPermissions;
        this.cacheEvictions = cacheEvictions;
    }

    @Transactional
    @Override
    public void saveOrUpdate(RoleEntity role, List<String> affectedCodes) {
        RoleEntity saved = roles.save(role);
        cacheEvictions.enqueue(List.of(), List.of(saved.getId()), affectedCodes, true, false);
    }

    @Transactional
    @Override
    public void delete(List<Long> ids, List<String> roleCodes) {
        roleMenus.deleteAllByRoleIdIn(ids);
        userRoles.deleteByRoleIdIn(ids);
        dataPermissions.deleteByRoleIdIn(ids);
        roles.deleteAllByIdInBatch(ids);
        cacheEvictions.enqueue(List.of(), ids, roleCodes, true, false);
    }
}
