package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wiki.chiu.micro.user.adapter.out.persistence.RoleWrapper;
import wiki.chiu.micro.user.application.model.RoleAuthorization;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.Role;
import wiki.chiu.micro.user.application.model.RoleDraft;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleReader roles;
    @Mock
    private RoleMenuReader roleMenus;
    @Mock
    private UserRoleReader userRoles;
    @Mock
    private RoleDataPermissionReader dataPermissions;
    @Mock
    private RoleWrapper roleWrapper;
    @Mock
    private AuthorizationQueryService authorizationQueries;
    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void delegatesBatchAuthorizationQuery() {
        List<Long> roleIds = List.of(7L, 8L, 7L);
        List<RoleAuthorization> expected =
            List.of(RoleAuthorization.missing(7L), RoleAuthorization.missing(8L));
        when(authorizationQueries.findRoleAuthorizations(roleIds)).thenReturn(expected);

        assertSame(expected, service.findRoleAuthorizations(roleIds));
    }

    @Test
    void saveRoleDoesNotReplaceDataPermissions() {
        Role existing = new Role(7L, null, "editor", null, null, null, null);
        when(roles.findById(7L)).thenReturn(Optional.of(existing));

        service.saveOrUpdate(new RoleDraft(7L, "Editor", "editor", "Edit blogs", 0));

        verify(roleWrapper).saveOrUpdate(any(Role.class), eq(List.of("editor")));
    }
}
