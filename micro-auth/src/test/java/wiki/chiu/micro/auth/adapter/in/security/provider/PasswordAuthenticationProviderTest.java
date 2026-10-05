package wiki.chiu.micro.auth.adapter.in.security.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import wiki.chiu.micro.auth.adapter.in.security.LoginUser;
import wiki.chiu.micro.auth.application.port.in.PasswordFailurePolicy;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

class PasswordAuthenticationProviderTest {

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final PasswordFailurePolicy passwordFailures = mock(PasswordFailurePolicy.class);
    private final PasswordAuthenticationProvider provider =
        new PasswordAuthenticationProvider(
            encoder, mock(UserDetailsService.class), mock(UserDirectory.class), passwordFailures);
    private final LoginUser user =
        new LoginUser("alice", "encoded", true, true, true, true, List.of(), 42L);

    @Test
    void mismatchedPasswordIsRecordedAndRejected() {
        when(encoder.matches("wrong", "encoded")).thenReturn(false);

        BadCredentialsException exception =
            assertThrows(
                BadCredentialsException.class,
                () -> provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", "wrong")));

        assertEquals(
            "Failed to authenticate since password does not match stored value", exception.getMessage());
        verify(passwordFailures).recordFailure(42L);
    }

    @Test
    void missingCredentialsAreRejectedWithoutRecordingAFailure() {
        BadCredentialsException exception =
            assertThrows(
                BadCredentialsException.class,
                () -> provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", null)));

        assertEquals(
            "Failed to authenticate since no credentials provided", exception.getMessage());
        verify(passwordFailures, never()).recordFailure(42L);
    }

    @Test
    void matchingPasswordIsAccepted() {
        when(encoder.matches("right", "encoded")).thenReturn(true);

        provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", "right"));

        verify(passwordFailures, never()).recordFailure(42L);
    }
}
