package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.wrapper.UserIdentityWrapper;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {

  @Mock private UserRepository users;
  @Mock private UserIdentityWrapper identityWrapper;
  @InjectMocks private UserIdentityServiceImpl service;

  @Test
  void accessSnapshotCombinesStatusAndRoleIds() {
    var first = accessRow(42L, 0, 7L);
    var second = accessRow(42L, 0, 8L);
    when(users.findAccessRows(42L)).thenReturn(List.of(first, second));

    var context = service.findUserAccess(42L);

    assertEquals(42L, context.userId());
    assertEquals(0, context.status());
    assertEquals(List.of(7L, 8L), context.roleIds());
    assertEquals(true, context.exists());
  }

  @Test
  void accessSnapshotPreservesDisabledStatusAndEmptyRoles() {
    var row = accessRow(42L, 1, null);
    when(users.findAccessRows(42L)).thenReturn(List.of(row));

    var context = service.findUserAccess(42L);

    assertEquals(1, context.status());
    assertEquals(List.of(), context.roleIds());
  }

  @Test
  void accessSnapshotRepresentsMissingUser() {
    when(users.findAccessRows(42L)).thenReturn(List.of());

    var context = service.findUserAccess(42L);

    assertEquals(false, context.exists());
    assertEquals(List.of(), context.roleIds());
  }

  private UserRepository.UserAccessRow accessRow(Long userId, Integer status, Long roleId) {
    return new UserRepository.UserAccessRow() {
      @Override
      public Long getUserId() {
        return userId;
      }

      @Override
      public Integer getStatus() {
        return status;
      }

      @Override
      public Long getRoleId() {
        return roleId;
      }
    };
  }
}
