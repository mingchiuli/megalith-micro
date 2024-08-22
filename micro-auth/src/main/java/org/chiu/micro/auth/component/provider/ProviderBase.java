package org.chiu.micro.auth.component.provider;

import org.chiu.micro.auth.dto.RoleEntityDto;
import org.chiu.micro.auth.rpc.wrapper.UserHttpServiceWrapper;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.chiu.micro.auth.lang.ExceptionMessage.*;
import static org.chiu.micro.auth.lang.StatusEnum.*;

import java.util.List;

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

        List<RoleEntityDto> roleEntities = userHttpServiceWrapper.findByRoleCodeInAndStatus(roles, NORMAL.getCode());
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
