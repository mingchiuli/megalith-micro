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
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.req.RoleEntityReq;

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
        List<RoleAuthorizationRpcVo> expected =
            List.of(RoleAuthorizationRpcVo.missing(7L), RoleAuthorizationRpcVo.missing(8L));
        when(authorizationQueries.findRoleAuthorizations(roleIds)).thenReturn(expected);

        assertSame(expected, service.findRoleAuthorizations(roleIds));
    }

    @Test
    void saveRoleDoesNotReplaceDataPermissions() {
        RoleEntity existing = RoleEntity.builder().id(7L).code("editor").build();
        when(roles.findById(7L)).thenReturn(Optional.of(existing));

        service.saveOrUpdate(new RoleEntityReq(Optional.of(7L), "Editor", "editor", "Edit blogs", 0));

        verify(roleWrapper).saveOrUpdate(any(RoleEntity.class), eq(List.of("editor")));
    }
}
