package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.ACCOUNT_LOCKED;
import static wiki.chiu.micro.common.error.ExceptionMessage.ROLE_DISABLED;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author mingchiuli
 * @create 2023-01-31 2:09 am
 */
public abstract sealed class ProviderBase extends DaoAuthenticationProvider
    permits EmailAuthenticationProvider, PasswordAuthenticationProvider, SMSAuthenticationProvider {

    protected ProviderBase(UserDetailsService userDetailsService) {
        super(userDetailsService);
        setHideUserNotFoundExceptions(false);
    }

    protected abstract void authProcess(UserDetails user, Authentication authentication);

    private void checkRoleStatus(UserDetails user) {
        if (user.getAuthorities().isEmpty()) {
            throw new BadCredentialsException(ROLE_DISABLED.getMsg());
        }
    }

    @Override
    protected void additionalAuthenticationChecks(
        @NonNull UserDetails userDetails, @NonNull UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        authProcess(userDetails, authentication);
    }

    @Override
    @NonNull
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails user =
            retrieveUser(
                authentication.getName(), (UsernamePasswordAuthenticationToken) authentication);
        if (!user.isAccountNonLocked()) {
            throw new LockedException(ACCOUNT_LOCKED.getMsg());
        }
        checkRoleStatus(user);
        additionalAuthenticationChecks(user, (UsernamePasswordAuthenticationToken) authentication);
        return createSuccessAuthentication(user, authentication, user);
    }
}
