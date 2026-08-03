package wiki.chiu.micro.auth.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
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
import wiki.chiu.micro.common.req.WebSocketTicketReq;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.error;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        AuthorityRouteReq.class,
        AuthorityRouteCheckReq.class,
        WebSocketTicketReq.class,
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
                                              AuthInternalHttpHandler internalHandler) {
        return routes(authHandler, tokenHandler, codeHandler, internalHandler);
    }

    public static RouterFunction<ServerResponse> routes(AuthHttpHandler authHandler,
                                                        TokenHttpHandler tokenHandler,
                                                        CodeHttpHandler codeHandler,
                                                        AuthInternalHttpHandler internalHandler) {
        RouterFunctions.Builder builder = route()
                .GET("/auth/menu/nav", authHandler::nav)
                .POST("/token/refresh", tokenHandler::refreshToken)
                .POST("/token/logout", tokenHandler::logout)
                .GET("/token/userinfo", tokenHandler::userinfo)
                .GET("/code/email", codeHandler::createEmailCode)
                .GET("/code/sms", codeHandler::createSmsCode)
                .GET("/inner/auth", internalHandler::getAuthentication)
                .POST("/inner/auth/route", internalHandler::getAuthorityRoute)
                .POST("/inner/auth/route/check", internalHandler::routeCheck);
        builder.POST("/inner/token/websocket", internalHandler::issueWebSocketTicket);
        builder.onError(BadCredentialsException.class,
                (exception, request) -> error(HttpStatus.UNAUTHORIZED, exception, log));
        builder.onError(AuthException.class,
                (exception, request) -> error(HttpStatus.FORBIDDEN, exception, log));
        builder.onError(AccessDeniedException.class,
                (exception, request) -> error(HttpStatus.FORBIDDEN, exception, log));
        return withDefaultErrorHandling(builder, log).build();
    }
}
