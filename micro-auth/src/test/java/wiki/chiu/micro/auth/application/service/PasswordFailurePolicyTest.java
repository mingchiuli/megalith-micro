package wiki.chiu.micro.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.application.port.out.PasswordFailureStore;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

class PasswordFailurePolicyTest {

    private final PasswordFailureStore failures = mock(PasswordFailureStore.class);
    private final UserDirectory users = mock(UserDirectory.class);
    private final PasswordFailurePolicy policy = new PasswordFailurePolicy(failures, users, 3);

    @Test
    void locksTheAccountOnceTheBudgetIsSpent() {
        when(failures.recordFailure(42L)).thenReturn(3L);

        assertTrue(policy.recordFailure(42L));

        verify(users).lockAfterPasswordFailures(42L);
    }

    @Test
    void keepsTheAccountOpenBelowTheBudget() {
        when(failures.recordFailure(42L)).thenReturn(2L);

        assertFalse(policy.recordFailure(42L));

        verify(users, never()).lockAfterPasswordFailures(42L);
    }

    @Test
    void rejectsANonPositiveBudget() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new PasswordFailurePolicy(failures, users, 0));
    }
}
