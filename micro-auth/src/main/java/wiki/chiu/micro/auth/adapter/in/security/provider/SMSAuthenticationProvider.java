package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_EXPIRED;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_MISMATCH;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_TRY_MAX;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.in.security.token.SMSAuthenticationToken;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore.Verification;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;

/**
 * @author mingchiuli
 * @create 2023-03-08 1:59 am
 */
@Component
public final class SMSAuthenticationProvider extends ProviderBase {

    private final LoginCodeStore codes;

    private final int maxAttempts;

    public SMSAuthenticationProvider(
        UserDetailsService userDetailsService,
        UserDirectory users,
        LoginCodeStore codes,
        @Value("${megalith.auth.code.max-attempts:3}") int maxAttempts) {
        super(userDetailsService, users);
        this.codes = codes;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return SMSAuthenticationToken.class.equals(authentication);
    }

    @Override
    protected void authProcess(UserDetails user, Authentication authentication) {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException(SMS_NOT_EXIST.getMsg());
        }
        Verification verification =
            codes.verify(
                LoginCodeStore.LoginChannel.PHONE,
                user.getUsername(),
                authentication.getCredentials().toString(),
                maxAttempts);
        if (verification != Verification.ACCEPTED) {
            throw new BadCredentialsException(messageOf(verification));
        }
    }

    private String messageOf(Verification verification) {
        return switch (verification) {
            case NOT_FOUND -> SMS_NOT_EXIST.getMsg();
            case MISMATCH -> SMS_MISMATCH.getMsg();
            case EXPIRED -> SMS_EXPIRED.getMsg();
            case ATTEMPTS_EXCEEDED -> SMS_TRY_MAX.getMsg();
            case ACCEPTED -> throw new IllegalStateException("Accepted codes have no failure message");
        };
    }
}
