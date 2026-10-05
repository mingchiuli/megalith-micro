package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.CODE_EXPIRED;
import static wiki.chiu.micro.common.error.ExceptionMessage.CODE_MISMATCH;
import static wiki.chiu.micro.common.error.ExceptionMessage.CODE_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.CODE_TRY_MAX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.in.security.token.EmailAuthenticationToken;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

/**
 * @author mingchiuli
 * @create 2022-12-30 10:57 am
 */
@Component
public final class EmailAuthenticationProvider extends ProviderBase {

    private final LoginCodeStore codes;

    private final int maxAttempts;

    public EmailAuthenticationProvider(
        UserDetailsService userDetailsService,
        UserDirectory users,
        LoginCodeStore codes,
        @Value("${megalith.auth.code.max-attempts:3}") int maxAttempts) {
        super(userDetailsService, users);
        this.codes = codes;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.equals(authentication);
    }

    @Override
    protected void authProcess(UserDetails user, Authentication authentication) {
        Verification verification =
            codes.verify(
                LoginCodeStore.LoginChannel.EMAIL,
                user.getUsername(),
                String.valueOf(authentication.getCredentials()),
                maxAttempts);
        if (verification != Verification.ACCEPTED) {
            throw new BadCredentialsException(messageOf(verification));
        }
    }

    private String messageOf(Verification verification) {
        return switch (verification) {
            case NOT_FOUND -> CODE_NOT_EXIST.getMsg();
            case MISMATCH -> CODE_MISMATCH.getMsg();
            case EXPIRED -> CODE_EXPIRED.getMsg();
            case ATTEMPTS_EXCEEDED -> CODE_TRY_MAX.getMsg();
            case ACCEPTED -> throw new IllegalStateException("Accepted codes have no failure message");
        };
    }
}
