package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.repository.RoleDataPermissionRepository;
import wiki.chiu.micro.user.repository.RoleMenuRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.wrapper.RoleWrapper;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

  @Mock private RoleRepository roles;
  @Mock private RoleMenuRepository roleMenus;
  @Mock private UserRoleRepository userRoles;
  @Mock private RoleDataPermissionRepository dataPermissions;
  @Mock private RoleWrapper roleWrapper;
  @InjectMocks private RoleServiceImpl service;

  @Test
  void batchAuthorizationGroupsJoinRowsAndRepresentsMissingRoles() {
    when(roles.findAuthorizationRowsByRoleIds(List.of(7L, 8L)))
        .thenReturn(
            List.of(
                row(7L, "editor", "blog_read", "BLOG_VIEW_ALL"),
                row(7L, "editor", "blog_write", "BLOG_VIEW_ALL"),
                row(7L, "editor", "blog_read", "BLOG_EDIT_ALL")));

    var result = service.findRoleAuthorizations(List.of(7L, 8L, 7L));

    assertEquals(2, result.size());
    assertTrue(result.getFirst().exists());
    assertEquals(Set.of("blog_read", "blog_write"), result.getFirst().authorityCodes());
    assertEquals(
        List.of(DataPermissionEnum.BLOG_VIEW_ALL, DataPermissionEnum.BLOG_EDIT_ALL),
        result.getFirst().dataPermissions());
    assertEquals(8L, result.getLast().roleId());
    assertFalse(result.getLast().exists());
  }

  private RoleRepository.RoleAuthorizationRow row(
      Long roleId, String code, String authorityCode, String permissionCode) {
    return new RoleRepository.RoleAuthorizationRow() {
      @Override
      public Long getRoleId() {
        return roleId;
      }

      @Override
      public String getCode() {
        return code;
      }

      @Override
      public Integer getStatus() {
        return StatusEnum.NORMAL.getCode();
      }

      @Override
      public String getAuthorityCode() {
        return authorityCode;
      }

      @Override
      public String getPermissionCode() {
        return permissionCode;
      }
    };
  }
}
