package wiki.chiu.micro.auth.adapter.in.security.provider;

import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_EXPIRED;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_MISMATCH;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_NOT_EXIST;
import static wiki.chiu.micro.common.error.ExceptionMessage.SMS_TRY_MAX;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.auth.adapter.in.security.token.SMSAuthenticationToken;
import wiki.chiu.micro.auth.application.model.CodeVerification;
import wiki.chiu.micro.auth.application.model.LoginChannel;
import wiki.chiu.micro.auth.application.port.in.LoginCodeAuthentication;

/**
 * @author mingchiuli
 * @create 2023-03-08 1:59 am
 */
@Component
public final class SMSAuthenticationProvider extends ProviderBase {

    private final LoginCodeAuthentication loginCodes;

    public SMSAuthenticationProvider(
        UserDetailsService userDetailsService, LoginCodeAuthentication loginCodes) {
        super(userDetailsService);
        this.loginCodes = loginCodes;
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
        CodeVerification verification =
            loginCodes.authenticate(
                LoginChannel.PHONE,
                user.getUsername(),
                authentication.getCredentials().toString());
        if (verification != CodeVerification.ACCEPTED) {
            throw new BadCredentialsException(messageOf(verification));
        }
    }

    private String messageOf(CodeVerification verification) {
        return switch (verification) {
            case NOT_FOUND -> SMS_NOT_EXIST.getMsg();
            case MISMATCH -> SMS_MISMATCH.getMsg();
            case EXPIRED -> SMS_EXPIRED.getMsg();
            case ATTEMPTS_EXCEEDED -> SMS_TRY_MAX.getMsg();
            case ACCEPTED -> throw new IllegalStateException("Accepted codes have no failure message");
        };
    }
}
