package wiki.chiu.micro.auth.adapter.in.security.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class SMSAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    public SMSAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
