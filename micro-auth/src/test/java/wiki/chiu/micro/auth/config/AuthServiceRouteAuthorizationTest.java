package wiki.chiu.micro.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.impl.AuthServiceImpl;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;

class AuthServiceRouteAuthorizationTest {

  private AuthWrapper authWrapper;
  private UserHttpServiceWrapper userHttpServiceWrapper;
  private JwtTokenService tokens;
  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    JwtProperties properties =
        new JwtProperties(
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
            900,
            604800,
            300,
            "micro-auth",
            "megalith-api");
    JwtConfig config = new JwtConfig();
    SecretKey secretKey = config.jwtSecretKey(properties);
    tokens =
        new JwtTokenService(
            config.jwtEncoder(secretKey),
            config.accessJwtDecoder(secretKey, properties),
            config.refreshJwtDecoder(secretKey, properties),
            config.websocketJwtDecoder(secretKey, properties),
            properties);

    authWrapper = mock(AuthWrapper.class);
    userHttpServiceWrapper = mock(UserHttpServiceWrapper.class);
    authService =
        new AuthServiceImpl(
            authWrapper,
            mock(RedissonClient.class),
            mock(TaskExecutor.class),
            mock(ResourceLoader.class),
            tokens,
            userHttpServiceWrapper);

    when(userHttpServiceWrapper.findById(42L))
        .thenReturn(UserEntityRpcVo.builder().id(42L).status(StatusEnum.NORMAL.getCode()).build());
    when(userHttpServiceWrapper.findRoleCodesByUserId(42L)).thenReturn(List.of("user"));
  }

  @Test
  void websocketTicketOnlyAuthorizesItsBoundRoom() {
    when(authWrapper.getAllSystemAuthorities())
        .thenReturn(List.of(authority("sync_room", "/rooms/**", AuthTypeEnum.NEED_AUTH)));

    String roomTicket = "Bearer " + tokens.issueWebSocketToken(42L, "blog-7");
    String accessToken = "Bearer " + tokens.issueAccessToken(42L);

    AuthorityRouteRpcVo route = authService.authorizeRoute(route("/rooms/blog-7"), roomTicket);
    assertEquals("sync", route.serviceHost());
    assertEquals(Integer.valueOf(8089), route.servicePort());
    assertThrows(
        MissException.class, () -> authService.authorizeRoute(route("/rooms/blog-8"), roomTicket));
    assertThrows(
        MissException.class, () -> authService.authorizeRoute(route("/rooms/blog-7"), accessToken));
  }

  @Test
  void exactProtectedRouteTakesPrecedenceOverWildcardWhitelist() {
    when(authWrapper.getAllSystemAuthorities())
        .thenReturn(
            List.of(
                authority("public_api", "/api/**", AuthTypeEnum.WHITE_LIST),
                authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));

    assertThrows(
        MissException.class, () -> authService.authorizeRoute(route("/api/private"), null));
    assertEquals("service", authService.authorizeRoute(route("/api/public"), null).serviceHost());
    assertThrows(
        MissException.class, () -> authService.authorizeRoute(route("/apix/public"), null));
  }

  @Test
  void invalidAccessTokenIsUnauthenticatedButMissingAuthorityIsForbidden() {
    when(authWrapper.getAllSystemAuthorities())
        .thenReturn(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
    when(authWrapper.getAuthoritiesByRoleCode("user")).thenReturn(Set.of("other_api"));

    MissException invalidToken =
        assertThrows(
            MissException.class,
            () -> authService.authorizeRoute(route("/api/private"), "Bearer invalid-token"));
    assertSame(ExceptionMessage.TOKEN_INVALID, invalidToken.errorCode());

    MissException missingAuthority =
        assertThrows(
            MissException.class,
            () ->
                authService.authorizeRoute(
                    route("/api/private"), "Bearer " + tokens.issueAccessToken(42L)));
    assertSame(ExceptionMessage.NO_AUTH, missingAuthority.errorCode());
  }

  @Test
  void authorizedRoleReceivesTheResolvedRoute() {
    when(authWrapper.getAllSystemAuthorities())
        .thenReturn(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
    when(authWrapper.getAuthoritiesByRoleCode("user")).thenReturn(Set.of("private_api"));

    AuthorityRouteRpcVo route =
        authService.authorizeRoute(route("/api/private"), "Bearer " + tokens.issueAccessToken(42L));

    assertEquals("service", route.serviceHost());
    assertEquals(Integer.valueOf(8080), route.servicePort());
  }

  @Test
  void userLookupFailureIsNotCollapsedIntoForbidden() {
    when(authWrapper.getAllSystemAuthorities())
        .thenReturn(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
    MissException failure = new MissException(ExceptionMessage.USER_NOT_EXIST);
    when(userHttpServiceWrapper.findById(42L)).thenThrow(failure);

    MissException actual =
        assertThrows(
            MissException.class,
            () ->
                authService.authorizeRoute(
                    route("/api/private"), "Bearer " + tokens.issueAccessToken(42L)));

    assertSame(failure, actual);
  }

  private AuthorityRouteReq route(String path) {
    return new AuthorityRouteReq("GET", path, null);
  }

  private AuthorityRpcVo authority(String code, String pattern, AuthTypeEnum type) {
    return AuthorityRpcVo.builder()
        .code(code)
        .methodType("GET")
        .routePattern(pattern)
        .serviceHost(pattern.startsWith("/rooms/") ? "sync" : "service")
        .servicePort(pattern.startsWith("/rooms/") ? 8089 : 8080)
        .type(type.getCode())
        .build();
  }
}
