package wiki.chiu.micro.auth.component.token;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class EmailAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public EmailAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}