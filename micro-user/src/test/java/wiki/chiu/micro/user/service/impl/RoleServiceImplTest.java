package wiki.chiu.micro.user.service.impl;

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
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.AuthorizationQueryService;
import wiki.chiu.micro.user.wrapper.RoleWrapper;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

  @Mock private RoleRepository roles;
  @Mock private RoleMenuRepository roleMenus;
  @Mock private UserRoleRepository userRoles;
  @Mock private RoleDataPermissionRepository dataPermissions;
  @Mock private RoleWrapper roleWrapper;
  @Mock private AuthorizationQueryService authorizationQueries;
  @InjectMocks private RoleServiceImpl service;

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
