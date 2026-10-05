package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.RoleDataPermission;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.domain.UserRole;

class UserRoleServiceImplTest {

    private final RoleReader roles = mock(RoleReader.class);
    private final UserRoleReader userRoles = mock(UserRoleReader.class);
    private final RoleDataPermissionReader permissions = mock(RoleDataPermissionReader.class);
    private final UserRoleServiceImpl service =
        new UserRoleServiceImpl(roles, userRoles, permissions);

    @Test
    void mergesAndDeduplicatesPermissionsFromEnabledRoles() {
        UserRole first = new UserRole(null, 1L, 10L, null, null);
        UserRole second = new UserRole(null, 1L, 11L, null, null);
        Role enabled = new Role(10L, null, null, null, null, null, StatusEnum.NORMAL.getCode());
        Role disabled = new Role(11L, null, null, null, null, null, StatusEnum.HIDE.getCode());
        when(userRoles.findByUserId(1L)).thenReturn(List.of(first, second));
        when(roles.findAllById(List.of(10L, 11L))).thenReturn(List.of(enabled, disabled));
        when(permissions.findByRoleIdIn(List.of(10L)))
            .thenReturn(
                List.of(
                    new RoleDataPermission(null, 10L, DataPermissionEnum.BLOG_VIEW_ALL, null, null),
                    new RoleDataPermission(null, 10L, DataPermissionEnum.BLOG_VIEW_ALL, null, null),
                    new RoleDataPermission(null, 10L, DataPermissionEnum.BLOG_DELETE_ALL, null, null)));

        assertEquals(
            List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_DELETE_ALL),
            service.findDataPermissionsByUserId(1L));
    }

    @Test
    void userWithoutEnabledRolesHasNoDataPermissions() {
        when(userRoles.findByUserId(1L)).thenReturn(List.of());
        when(roles.findAllById(List.of())).thenReturn(List.of());

        assertEquals(List.of(), service.findDataPermissionsByUserId(1L));
        verifyNoInteractions(permissions);
    }
}
