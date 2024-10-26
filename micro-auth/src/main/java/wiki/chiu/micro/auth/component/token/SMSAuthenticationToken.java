package wiki.chiu.micro.auth.component.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serial;

public class SMSAuthenticationToken extends UsernamePasswordAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 6200L;

    public SMSAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}