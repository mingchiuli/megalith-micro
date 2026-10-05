package wiki.chiu.micro.user.adapter.out.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.user.adapter.out.messaging.AuthCacheEvictionOutbox;
import wiki.chiu.micro.user.adapter.out.persistence.entity.RoleEntity;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleMenuRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.RoleRepository;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRoleRepository;
import wiki.chiu.micro.user.domain.Role;

class RoleWrapperTest {

    @Test
    void savingRoleDoesNotReplaceDataPermissions() {
        RoleRepository roles = mock(RoleRepository.class);
        RoleMenuRepository roleMenus = mock(RoleMenuRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        RoleDataPermissionRepository dataPermissions = mock(RoleDataPermissionRepository.class);
        AuthCacheEvictionOutbox cacheEvictions = mock(AuthCacheEvictionOutbox.class);
        RoleWrapper wrapper =
            new RoleWrapper(roles, roleMenus, userRoles, dataPermissions, cacheEvictions);
        Role role = new Role(7L, "name", "editor", null, null, null, 0);
        when(roles.save(any())).thenAnswer(invocation -> invocation.getArgument(0, RoleEntity.class));

        wrapper.saveOrUpdate(role, List.of("editor"));

        verify(dataPermissions, never()).deleteByRoleId(7L);
        verify(cacheEvictions).enqueue(List.of(), List.of(7L), List.of("editor"), true, false);
    }
}
