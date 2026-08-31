package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

class UserRoleServiceImplTest {

    private final RoleReader roles = mock(RoleReader.class);
    private final UserRoleReader userRoles = mock(UserRoleReader.class);
    private final RoleDataPermissionReader permissions = mock(RoleDataPermissionReader.class);
    private final UserRoleServiceImpl service =
        new UserRoleServiceImpl(roles, userRoles, permissions);

    @Test
    void mergesAndDeduplicatesPermissionsFromEnabledRoles() {
        UserRoleEntity first = UserRoleEntity.builder().roleId(10L).userId(1L).build();
        UserRoleEntity second = UserRoleEntity.builder().roleId(11L).userId(1L).build();
        RoleEntity enabled = RoleEntity.builder().id(10L).status(StatusEnum.NORMAL.getCode()).build();
        RoleEntity disabled = RoleEntity.builder().id(11L).status(StatusEnum.HIDE.getCode()).build();
        when(userRoles.findByUserId(1L)).thenReturn(List.of(first, second));
        when(roles.findAllById(List.of(10L, 11L))).thenReturn(List.of(enabled, disabled));
        when(permissions.findByRoleIdIn(List.of(10L)))
            .thenReturn(
                List.of(
                    new RoleDataPermissionEntity(10L, DataPermissionEnum.BLOG_VIEW_ALL),
                    new RoleDataPermissionEntity(10L, DataPermissionEnum.BLOG_VIEW_ALL),
                    new RoleDataPermissionEntity(10L, DataPermissionEnum.BLOG_DELETE_ALL)));

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
