package wiki.chiu.micro.auth.component.manager;

import wiki.chiu.micro.auth.component.token.EmailAuthenticationToken;
import wiki.chiu.micro.auth.component.token.SMSAuthenticationToken;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import wiki.chiu.micro.common.lang.Const;

import static wiki.chiu.micro.common.lang.ExceptionMessage.INVALID_LOGIN_OPERATE;

import java.util.List;

public class CustomProviderManager extends ProviderManager {

    public CustomProviderManager(List<AuthenticationProvider> providers) {
        super(providers);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        authentication = getAuthGrantTypeToken(authentication);

        for (AuthenticationProvider provider : getProviders()) {
            if (provider.supports(authentication.getClass())) {
                return provider.authenticate(authentication);
            }
        }
        throw new BadCredentialsException(INVALID_LOGIN_OPERATE.getMsg());
    }

    private Authentication getAuthGrantTypeToken(Authentication authentication) {
        String username = authentication.getName();

        if (Const.EMAIL_PATTERN.matcher(username).matches()) {
            return new EmailAuthenticationToken(username, authentication.getCredentials());
        } else if (Const.PHONE_PATTERN.matcher(username).matches()) {
            return new SMSAuthenticationToken(username, authentication.getCredentials());
        } else {
            return authentication;
        }
    }
}
