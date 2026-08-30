package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wiki.chiu.micro.user.adapter.out.persistence.UserIdentityWrapper;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.config.PasswordLockProperties;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {

  @Mock private UserReader users;
  @Mock private UserIdentityWrapper identityWrapper;
  @Mock private AuthorizationQueryService authorizationQueries;
  @Mock private PasswordLockProperties passwordLockProperties;
  @InjectMocks private UserIdentityServiceImpl service;

  @Test
  void delegatesAccessSnapshotQuery() {
    var expected = new UserAccessRpcVo(42L, true, 0, List.of(7L, 8L));
    when(authorizationQueries.findUserAccess(42L)).thenReturn(expected);

    assertSame(expected, service.findUserAccess(42L));
  }

  @Test
  void selectsExpiredIdsBeforeDelegatingTheConditionalWrite() {
    when(passwordLockProperties.getBatchSize()).thenReturn(100);
    when(users.findExpiredPasswordLockIds(any(), anyInt())).thenReturn(List.of(7L, 8L));
    when(identityWrapper.unlockExpired(List.of(7L, 8L))).thenReturn(2);

    assertEquals(2, service.unlockExpiredBatch());

    verify(identityWrapper).unlockExpired(List.of(7L, 8L));
  }
}
