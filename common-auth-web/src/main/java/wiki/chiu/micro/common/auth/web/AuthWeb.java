package wiki.chiu.micro.common.auth.web;

import org.springframework.web.servlet.function.ServerRequest;

import wiki.chiu.micro.common.security.AuthPrincipal;

public final class AuthWeb {

    private AuthWeb() {
    }

    public static AuthPrincipal authPrincipal(ServerRequest request) {
        return AuthPrincipalCodec.decode(request.headers().firstHeader(AuthPrincipalCodec.HEADER_NAME));
    }
}
