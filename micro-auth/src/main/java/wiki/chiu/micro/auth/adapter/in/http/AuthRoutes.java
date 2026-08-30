package wiki.chiu.micro.auth.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.error;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

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
import wiki.chiu.micro.auth.api.AuthHttpPaths;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.dto.CodeReq;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.security.AuthPrincipal;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
  Result.class,
  AuthorityRouteReq.class,
  WebSocketTicketReq.class,
  MenuWithChildVo.class,
  UserInfoVo.class,
  AuthorityRouteRpcVo.class,
  AuthPrincipal.class,
  DataPermissionEnum.class,
  CodeReq.class
})
public class AuthRoutes {

  private static final Logger log = LoggerFactory.getLogger(AuthRoutes.class);

  @Bean
  RouterFunction<ServerResponse> authRouter(
      AuthHttpHandler authHandler,
      TokenHttpHandler tokenHandler,
      CodeHttpHandler codeHandler,
      AuthInternalHttpHandler internalHandler) {
    return routes(authHandler, tokenHandler, codeHandler, internalHandler);
  }

  public static RouterFunction<ServerResponse> routes(
      AuthHttpHandler authHandler,
      TokenHttpHandler tokenHandler,
      CodeHttpHandler codeHandler,
      AuthInternalHttpHandler internalHandler) {
    RouterFunctions.Builder builder =
        route()
            .GET("/auth/menu/nav", authHandler::nav)
            .POST("/token/refresh", tokenHandler::refreshToken)
            .POST("/token/logout", tokenHandler::logout)
            .GET("/token/userinfo", tokenHandler::userinfo)
            .POST("/code/email", codeHandler::createEmailCode)
            .POST("/code/sms", codeHandler::createSmsCode)
            .POST("/inner" + AuthHttpPaths.AUTH_ROUTE, internalHandler::getAuthorityRoute);
    builder.POST("/inner" + AuthHttpPaths.WEBSOCKET_TOKEN, internalHandler::issueWebSocketTicket);
    builder.onError(
        BadCredentialsException.class,
        (exception, request) -> error(HttpStatus.UNAUTHORIZED, exception, log));
    builder.onError(
        AuthException.class, (exception, request) -> error(HttpStatus.FORBIDDEN, exception, log));
    builder.onError(
        AccessDeniedException.class,
        (exception, request) -> error(HttpStatus.FORBIDDEN, exception, log));
    return withDefaultErrorHandling(builder, log).build();
  }
}
