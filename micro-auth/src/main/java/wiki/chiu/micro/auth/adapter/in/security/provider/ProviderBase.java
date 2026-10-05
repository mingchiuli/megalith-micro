package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.ACCOUNT_LOCKED;
import static wiki.chiu.micro.common.error.ExceptionMessage.ROLE_DISABLED;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import wiki.chiu.micro.auth.application.port.out.UserDirectory;

/**
 * @author mingchiuli
 * @create 2023-01-31 2:09 am
 */
public abstract sealed class ProviderBase extends DaoAuthenticationProvider
    permits EmailAuthenticationProvider, PasswordAuthenticationProvider, SMSAuthenticationProvider {

    private final UserDirectory users;

    protected ProviderBase(UserDetailsService userDetailsService, UserDirectory users) {
        super(userDetailsService);
        setHideUserNotFoundExceptions(false);
        this.users = users;
    }

    protected abstract void authProcess(UserDetails user, Authentication authentication);

    private void checkRoleStatus(UserDetails user) {
        List<String> roles =
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if (users.findEnabledRoleCodes(roles).isEmpty()) {
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
