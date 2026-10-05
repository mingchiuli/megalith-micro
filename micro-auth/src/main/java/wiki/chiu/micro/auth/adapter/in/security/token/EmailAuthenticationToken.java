package wiki.chiu.micro.auth.adapter.in.security.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class EmailAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    public EmailAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
