package wiki.chiu.micro.auth.adapter.in.security.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

import wiki.chiu.micro.auth.adapter.in.security.LoginUser;
import wiki.chiu.micro.auth.application.port.in.PasswordAuthentication;

class PasswordAuthenticationProviderTest {

    private final PasswordAuthentication passwordAuthentication = mock(PasswordAuthentication.class);
    private final PasswordAuthenticationProvider provider =
        new PasswordAuthenticationProvider(mock(UserDetailsService.class), passwordAuthentication);
    private final LoginUser user =
        new LoginUser("alice", "encoded", true, true, true, true, List.of(), 42L);

    @Test
    void mismatchedPasswordIsRejected() {
        when(passwordAuthentication.authenticate(42L, "encoded", "wrong")).thenReturn(false);

        BadCredentialsException exception =
            assertThrows(
                BadCredentialsException.class,
                () -> provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", "wrong")));

        assertEquals(
            "Failed to authenticate since password does not match stored value", exception.getMessage());
    }

    @Test
    void missingCredentialsAreRejectedWithoutCallingTheUseCase() {
        BadCredentialsException exception =
            assertThrows(
                BadCredentialsException.class,
                () -> provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", null)));

        assertEquals(
            "Failed to authenticate since no credentials provided", exception.getMessage());
        verifyNoInteractions(passwordAuthentication);
    }

    @Test
    void matchingPasswordIsAccepted() {
        when(passwordAuthentication.authenticate(42L, "encoded", "right")).thenReturn(true);

        provider.authProcess(user, new UsernamePasswordAuthenticationToken("alice", "right"));

        verify(passwordAuthentication).authenticate(42L, "encoded", "right");
    }
}
