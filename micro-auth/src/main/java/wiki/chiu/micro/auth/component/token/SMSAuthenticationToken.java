package wiki.chiu.micro.auth.component.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


public class SMSAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public SMSAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}