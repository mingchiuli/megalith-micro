package wiki.chiu.micro.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.application.port.out.PasswordHasher;

class PasswordAuthenticationServiceImplTest {

    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final PasswordFailurePolicy passwordFailures = mock(PasswordFailurePolicy.class);
    private final PasswordAuthenticationServiceImpl service =
        new PasswordAuthenticationServiceImpl(hasher, passwordFailures);

    @Test
    void matchingPasswordIsAcceptedWithoutRecordingFailure() {
        when(hasher.matches("right", "encoded")).thenReturn(true);

        assertThat(service.authenticate(42L, "encoded", "right")).isTrue();

        verify(passwordFailures, never()).recordFailure(anyLong());
    }

    @Test
    void mismatchedPasswordIsRecordedAndRejected() {
        when(hasher.matches("wrong", "encoded")).thenReturn(false);

        assertThat(service.authenticate(42L, "encoded", "wrong")).isFalse();

        verify(passwordFailures).recordFailure(42L);
    }
}
