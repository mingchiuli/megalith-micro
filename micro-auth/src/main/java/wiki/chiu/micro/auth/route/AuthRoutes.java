package wiki.chiu.micro.auth.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.handler.AuthHttpHandler;
import wiki.chiu.micro.auth.handler.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.handler.CodeHttpHandler;
import wiki.chiu.micro.auth.handler.TokenHttpHandler;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.rpc.config.auth.AuthInfo;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.function.Function;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.authInfo;
import static wiki.chiu.micro.common.web.FunctionalWeb.errorMessage;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredHeader;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        AuthorityRouteReq.class,
        AuthorityRouteCheckReq.class,
        MenuWithChildVo.class,
        UserInfoVo.class,
        AuthRpcVo.class,
        AuthorityRouteRpcVo.class
})
public class AuthRoutes {

    private static final Logger log = LoggerFactory.getLogger(AuthRoutes.class);

    @Bean
    RouterFunction<ServerResponse> authRouter(AuthHttpHandler authHandler,
                                              TokenHttpHandler tokenHandler,
                                              CodeHttpHandler codeHandler,
                                              AuthInternalHttpHandler internalHandler,
                                              ValidatedRequest validation) {
        return routes(authHandler, tokenHandler, codeHandler, internalHandler,
                request -> authInfo(request, internalHandler), validation);
    }

    public static RouterFunction<ServerResponse> routes(AuthHttpHandler authHandler,
                                                        TokenHttpHandler tokenHandler,
                                                        CodeHttpHandler codeHandler,
                                                        AuthInternalHttpHandler internalHandler,
                                                        Function<ServerRequest, AuthInfo> authResolver,
                                                        ValidatedRequest validation) {
        return route()
                .GET("/auth/menu/nav", request -> ok(authHandler.nav(authResolver.apply(request))))
                .GET("/token/refresh", request -> ok(tokenHandler.refreshToken(authResolver.apply(request))))
                .GET("/token/userinfo", request -> ok(tokenHandler.userinfo(authResolver.apply(request))))
                .GET("/code/email", request -> ok(codeHandler.createEmailCode(
                        requiredParam(request, "loginName"))))
                .GET("/code/sms", request -> ok(codeHandler.createSmsCode(
                        requiredParam(request, "loginName"))))
                .GET("/inner/auth", request -> ok(internalHandler.getAuthentication(
                        requiredHeader(request, HttpHeaders.AUTHORIZATION))))
                .POST("/inner/auth/route", request -> ok(internalHandler.getAuthorityRoute(
                        validation.body(request, AuthorityRouteReq.class),
                        requiredHeader(request, HttpHeaders.AUTHORIZATION))))
                .POST("/inner/auth/route/check", request -> ok(internalHandler.routeCheck(
                        validation.body(request, AuthorityRouteCheckReq.class),
                        requiredHeader(request, HttpHeaders.AUTHORIZATION))))
                .filter(authErrors())
                .build();
    }

    private static HandlerFilterFunction<ServerResponse, ServerResponse> authErrors() {
        return (request, next) -> {
            try {
                return next.handle(request);
            }
            catch (Exception exception) {
                log.error("HTTP handler exception", exception);
                HttpStatus status = switch (exception) {
                    case BadCredentialsException ignored -> HttpStatus.UNAUTHORIZED;
                    case AuthException ignored -> HttpStatus.FORBIDDEN;
                    case AccessDeniedException ignored -> HttpStatus.FORBIDDEN;
                    default -> HttpStatus.BAD_REQUEST;
                };
                return ServerResponse.status(status).body(Result.fail(errorMessage(exception)));
            }
        };
    }
}
