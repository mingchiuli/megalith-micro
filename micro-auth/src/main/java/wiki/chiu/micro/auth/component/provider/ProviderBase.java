package wiki.chiu.micro.auth.component.provider;

import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.common.dto.RoleEntityRpcDto;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ACCOUNT_LOCKED;
import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_DISABLED;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;


/**
 * @author mingchiuli
 * @create 2023-01-31 2:09 am
 */
public abstract sealed class ProviderBase extends DaoAuthenticationProvider permits
        EmailAuthenticationProvider,
        PasswordAuthenticationProvider,
        SMSAuthenticationProvider {
    protected UserDetailsService userDetailsService;

    protected UserHttpServiceWrapper userHttpServiceWrapper;

    protected ProviderBase(UserDetailsService userDetailsService,
                           UserHttpServiceWrapper userHttpServiceWrapper) {
        setUserDetailsService(userDetailsService);
        setHideUserNotFoundExceptions(false);
        this.userDetailsService = userDetailsService;
        this.userHttpServiceWrapper = userHttpServiceWrapper;
    }

    protected abstract void authProcess(UserDetails user, Authentication authentication);

    private void checkRoleStatus(UserDetails user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<RoleEntityRpcDto> roleEntities = userHttpServiceWrapper.findByRoleCodeInAndStatus(roles, NORMAL.getCode());
        if (roleEntities.isEmpty()) {
            throw new BadCredentialsException(ROLE_DISABLED.getMsg());
        }
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        authProcess(userDetails, authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails user = retrieveUser(authentication.getName(), (UsernamePasswordAuthenticationToken) authentication);
        if (!user.isAccountNonLocked()) {
            throw new LockedException(ACCOUNT_LOCKED.getMsg());
        }
        checkRoleStatus(user);
        additionalAuthenticationChecks(user, (UsernamePasswordAuthenticationToken) authentication);
        return createSuccessAuthentication(user, authentication, user);
    }
}
