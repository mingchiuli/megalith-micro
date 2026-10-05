package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.PASSWORD_MISMATCH;
import static wiki.chiu.micro.common.error.ExceptionMessage.PASSWORD_MISS;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.in.security.LoginUser;
import wiki.chiu.micro.auth.application.port.in.PasswordFailurePolicy;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

/**
 * @author mingchiuli
 * @create 2023-01-14 9:02
 */
@Component
public final class PasswordAuthenticationProvider extends ProviderBase {

    private final PasswordEncoder passwordEncoder;

    private final PasswordFailurePolicy passwordFailures;

    public PasswordAuthenticationProvider(
        PasswordEncoder passwordEncoder,
        UserDetailsService userDetailsService,
        UserDirectory users,
        PasswordFailurePolicy passwordFailures) {
        super(userDetailsService, users);
        this.passwordEncoder = passwordEncoder;
        this.passwordFailures = passwordFailures;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }

    @Override
    protected void authProcess(UserDetails user, Authentication authentication) {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException(PASSWORD_MISS.getMsg());
        }
        if (!passwordEncoder.matches(
            authentication.getCredentials().toString(), user.getPassword())) {
            passwordFailures.recordFailure(((LoginUser) user).getUserId());
            throw new BadCredentialsException(PASSWORD_MISMATCH.getMsg());
        }
    }
}
