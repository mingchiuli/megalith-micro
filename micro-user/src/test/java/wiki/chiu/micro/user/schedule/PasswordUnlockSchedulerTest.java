package wiki.chiu.micro.user.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;
import wiki.chiu.micro.user.config.PasswordLockProperties;

class PasswordUnlockSchedulerTest {

    @Test
    void skipsWorkWhenAnotherNodeOwnsTheTaskLock() {
        RedisTaskLock taskLock = Mockito.mock(RedisTaskLock.class);
        UserIdentityService identities = Mockito.mock(UserIdentityService.class);
        PasswordLockProperties properties = new PasswordLockProperties();
        PasswordUnlockScheduler scheduler =
            new PasswordUnlockScheduler(taskLock, identities, properties, new SimpleMeterRegistry());
        when(taskLock.tryRun(eq("user:password-unlock"), any(Runnable.class))).thenReturn(false);

        scheduler.unlockExpired();

        verify(identities, never()).unlockExpiredBatch();
    }

    @Test
    void drainsFullBatchesWhileHoldingTheTaskLock() {
        RedisTaskLock taskLock = Mockito.mock(RedisTaskLock.class);
        UserIdentityService identities = Mockito.mock(UserIdentityService.class);
        PasswordLockProperties properties = new PasswordLockProperties();
        properties.setBatchSize(2);
        properties.setMaxBatchesPerRun(3);
        PasswordUnlockScheduler scheduler =
            new PasswordUnlockScheduler(taskLock, identities, properties, new SimpleMeterRegistry());
        when(taskLock.tryRun(eq("user:password-unlock"), any(Runnable.class)))
            .thenAnswer(
                invocation -> {
                    invocation.getArgument(1, Runnable.class).run();
                    return true;
                });
        when(identities.unlockExpiredBatch()).thenReturn(2, 2, 1);

        scheduler.unlockExpired();

        verify(identities, Mockito.times(3)).unlockExpiredBatch();
    }
}
