package wiki.chiu.micro.auth.component;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public final class LoginAuthenticationFilter extends AuthenticationFilter {

    public LoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginAuthenticationConverter authenticationConverter,
            LoginSuccessHandler successHandler,
            LoginFailureHandler failureHandler) {
        super(authenticationManager, authenticationConverter);
        setRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login"));
        setSuccessHandler(successHandler);
        setFailureHandler(failureHandler);
    }
}
