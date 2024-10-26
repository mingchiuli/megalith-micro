package wiki.chiu.micro.auth.component.token;


import java.io.Serial;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class EmailAuthenticationToken extends UsernamePasswordAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 6201L;

    public EmailAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}