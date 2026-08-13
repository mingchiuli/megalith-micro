package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.UserRoleService;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {

  @Mock private UserRepository users;
  @Mock private UserRoleService userRoles;
  @InjectMocks private UserIdentityServiceImpl service;

  @Test
  void authContextCombinesStatusAndActiveRoles() {
    when(users.findById(42L))
        .thenReturn(Optional.of(UserEntity.builder().id(42L).status(0).build()));
    when(userRoles.findRoleCodesByUserId(42L)).thenReturn(List.of("user", "editor"));

    var context = service.findAuthContext(42L);

    assertEquals(42L, context.userId());
    assertEquals(0, context.status());
    assertEquals(List.of("user", "editor"), context.roles());
  }

  @Test
  void authContextPreservesDisabledStatusAndEmptyRoles() {
    when(users.findById(42L))
        .thenReturn(Optional.of(UserEntity.builder().id(42L).status(1).build()));
    when(userRoles.findRoleCodesByUserId(42L)).thenReturn(List.of());

    var context = service.findAuthContext(42L);

    assertEquals(1, context.status());
    assertEquals(List.of(), context.roles());
  }

  @Test
  void authContextRejectsMissingUser() {
    when(users.findById(42L)).thenReturn(Optional.empty());

    assertThrows(MissException.class, () -> service.findAuthContext(42L));
  }
}
