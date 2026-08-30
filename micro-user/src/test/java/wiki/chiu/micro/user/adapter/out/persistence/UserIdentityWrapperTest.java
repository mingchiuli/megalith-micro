package wiki.chiu.micro.user.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static wiki.chiu.micro.common.lang.StatusEnum.HIDE;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import wiki.chiu.micro.user.adapter.out.persistence.repository.UserRepository;
import wiki.chiu.micro.user.config.PasswordLockProperties;
import wiki.chiu.micro.user.support.AuthCacheEvictionOutbox;

class UserIdentityWrapperTest {

  private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
  private final AuthCacheEvictionOutbox cacheEvictions =
      org.mockito.Mockito.mock(AuthCacheEvictionOutbox.class);
  private final PasswordLockProperties properties = new PasswordLockProperties();
  private final UserIdentityWrapper wrapper =
      new UserIdentityWrapper(users, cacheEvictions, properties, new SimpleMeterRegistry());

  @Test
  void locksForConfiguredDurationAndInvalidatesTheUserSnapshot() {
    when(users.lockAfterPasswordFailures(42L, NORMAL.getCode(), HIDE.getCode(), 900L))
        .thenReturn(1);

    wrapper.lockAfterPasswordFailures(42L);

    verify(cacheEvictions).enqueue(List.of(42L), List.of(), List.of(), false, false);
  }

  @Test
  void doesNotPublishWhenTheAccountWasAlreadyDisabled() {
    when(users.lockAfterPasswordFailures(42L, NORMAL.getCode(), HIDE.getCode(), 900L))
        .thenReturn(0);

    wrapper.lockAfterPasswordFailures(42L);

    verify(cacheEvictions, never()).enqueue(any(), any(), any(), eq(false), eq(false));
  }

  @Test
  void conditionallyUnlocksPreparedIdsAndInvalidatesTheirSnapshots() {
    when(users.unlockExpiredPasswordLocks(List.of(7L, 8L), HIDE.getCode(), NORMAL.getCode()))
        .thenReturn(2);

    int unlocked = wrapper.unlockExpired(List.of(7L, 8L));

    assertEquals(2, unlocked);
    verify(cacheEvictions).enqueue(List.of(7L, 8L), List.of(), List.of(), false, false);
  }

  @Test
  void emptyUnlockBatchDoesNotTouchPersistence() {
    assertEquals(0, wrapper.unlockExpired(List.of()));

    verify(users, never()).unlockExpiredPasswordLocks(any(), any(), any());
    verify(cacheEvictions, never()).enqueue(any(), any(), any(), eq(false), eq(false));
  }
}
