package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.domain.RoleDataPermissionEntity;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.RoleMenuEntity;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;

@ExtendWith(MockitoExtension.class)
class AuthorizationQueryServiceTest {

  @Mock private UserReader users;
  @Mock private UserRoleReader userRoles;
  @Mock private RoleReader roles;
  @Mock private RoleMenuReader roleMenus;
  @Mock private MenuAuthorityReader menuAuthorities;
  @Mock private AuthorityReader authorities;
  @Mock private RoleDataPermissionReader dataPermissions;
  @InjectMocks private AuthorizationQueryService queries;

  @Test
  void userAccessCombinesUserAndDistinctRoleIds() {
    var user =
        UserEntity.builder().id(42L).status(StatusEnum.HIDE.getCode()).username("reader").build();
    when(users.findById(42L)).thenReturn(Optional.of(user));
    when(userRoles.findByUserId(42L))
        .thenReturn(List.of(userRole(42L, 7L), userRole(42L, 8L), userRole(42L, 7L)));

    var result = queries.findUserAccess(42L);

    assertEquals(42L, result.userId());
    assertTrue(result.exists());
    assertEquals(StatusEnum.HIDE.getCode(), result.status());
    assertEquals(List.of(7L, 8L), result.roleIds());
  }

  @Test
  void missingUserDoesNotQueryRoles() {
    when(users.findById(42L)).thenReturn(Optional.empty());

    var result = queries.findUserAccess(42L);

    assertFalse(result.exists());
    assertEquals(List.of(), result.roleIds());
    verify(userRoles, never()).findByUserId(42L);
  }

  @Test
  void batchAuthorizationComposesSingleTableResults() {
    when(roles.findAllById(List.of(7L, 8L, 9L)))
        .thenReturn(List.of(role(7L, "editor"), role(9L, "auditor")));
    when(roleMenus.findByRoleIdIn(List.of(7L, 9L)))
        .thenReturn(List.of(roleMenu(7L, 100L), roleMenu(7L, 101L)));
    when(menuAuthorities.findByMenuIdIn(List.of(100L, 101L)))
        .thenReturn(
            List.of(
                menuAuthority(100L, 1L),
                menuAuthority(101L, 1L),
                menuAuthority(101L, 2L),
                menuAuthority(101L, 3L)));
    when(authorities.findByIdInAndStatus(List.of(1L, 2L, 3L), StatusEnum.NORMAL.getCode()))
        .thenReturn(List.of(authority(1L, "blog_read"), authority(2L, "blog_write")));
    when(dataPermissions.findByRoleIdIn(List.of(7L, 9L)))
        .thenReturn(
            List.of(
                permission(7L, DataPermissionEnum.BLOG_EDIT_ALL),
                permission(7L, DataPermissionEnum.BLOG_VIEW_ALL),
                permission(7L, DataPermissionEnum.BLOG_EDIT_ALL)));

    var result = queries.findRoleAuthorizations(List.of(7L, 8L, 7L, 9L));

    assertEquals(List.of(7L, 8L, 9L), result.stream().map(item -> item.roleId()).toList());
    assertTrue(result.getFirst().exists());
    assertEquals(Set.of("blog_read", "blog_write"), result.getFirst().authorityCodes());
    assertEquals(
        List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_EDIT_ALL),
        result.getFirst().dataPermissions());
    assertFalse(result.get(1).exists());
    assertTrue(result.getLast().exists());
    assertEquals(Set.of(), result.getLast().authorityCodes());
    assertEquals(List.of(), result.getLast().dataPermissions());
  }

  @Test
  void missingRolesDoNotQueryAssociationTables() {
    when(roles.findAllById(List.of(88L))).thenReturn(List.of());

    var result = queries.findRoleAuthorizations(List.of(88L));

    assertFalse(result.getFirst().exists());
    verify(roleMenus, never()).findByRoleIdIn(List.of(88L));
    verify(dataPermissions, never()).findByRoleIdIn(List.of(88L));
  }

  @Test
  void roleWithoutMenusDoesNotQueryMenuAuthorities() {
    when(roles.findAllById(List.of(9L))).thenReturn(List.of(role(9L, "auditor")));
    when(roleMenus.findByRoleIdIn(List.of(9L))).thenReturn(List.of());
    when(dataPermissions.findByRoleIdIn(List.of(9L))).thenReturn(List.of());

    var result = queries.findRoleAuthorizations(List.of(9L)).getFirst();

    assertTrue(result.exists());
    assertEquals(Set.of(), result.authorityCodes());
    verify(menuAuthorities, never()).findByMenuIdIn(anyList());
    verify(authorities, never()).findByIdInAndStatus(anyList(), eq(StatusEnum.NORMAL.getCode()));
  }

  @Test
  void allRoleAuthorizationsComposesSingleTableResults() {
    when(roles.findAll()).thenReturn(List.of(role(7L, "editor"), role(8L, "reader")));
    when(roleMenus.findByRoleIdIn(List.of(7L, 8L))).thenReturn(List.of(roleMenu(7L, 100L)));
    when(menuAuthorities.findByMenuIdIn(List.of(100L)))
        .thenReturn(List.of(menuAuthority(100L, 1L)));
    when(authorities.findByIdInAndStatus(List.of(1L), StatusEnum.NORMAL.getCode()))
        .thenReturn(List.of(authority(1L, "blog_read")));
    when(dataPermissions.findByRoleIdIn(List.of(7L, 8L))).thenReturn(List.of());

    var result = queries.findAllRoleAuthorizations();

    assertEquals(List.of(7L, 8L), result.stream().map(item -> item.roleId()).toList());
    assertTrue(result.getFirst().exists());
    assertEquals(Set.of("blog_read"), result.getFirst().authorityCodes());
    assertTrue(result.getLast().exists());
    assertEquals(Set.of(), result.getLast().authorityCodes());
  }

  @Test
  void emptyRoleTableReturnsNoAuthorizations() {
    when(roles.findAll()).thenReturn(List.of());

    assertEquals(List.of(), queries.findAllRoleAuthorizations());

    verify(roleMenus, never()).findByRoleIdIn(anyList());
  }

  @Test
  void emptyRoleIdsDoNotQueryRepositories() {
    assertEquals(List.of(), queries.findRoleAuthorizations(List.of()));

    verify(roles, never()).findAllById(List.of());
  }

  private UserRoleEntity userRole(Long userId, Long roleId) {
    return UserRoleEntity.builder().userId(userId).roleId(roleId).build();
  }

  private RoleEntity role(Long id, String code) {
    return RoleEntity.builder().id(id).code(code).status(StatusEnum.NORMAL.getCode()).build();
  }

  private RoleMenuEntity roleMenu(Long roleId, Long menuId) {
    return RoleMenuEntity.builder().roleId(roleId).menuId(menuId).build();
  }

  private MenuAuthorityEntity menuAuthority(Long menuId, Long authorityId) {
    return MenuAuthorityEntity.builder().menuId(menuId).authorityId(authorityId).build();
  }

  private AuthorityEntity authority(Long id, String code) {
    return AuthorityEntity.builder().id(id).code(code).status(StatusEnum.NORMAL.getCode()).build();
  }

  private RoleDataPermissionEntity permission(Long roleId, DataPermissionEnum permission) {
    return new RoleDataPermissionEntity(roleId, permission);
  }
}
