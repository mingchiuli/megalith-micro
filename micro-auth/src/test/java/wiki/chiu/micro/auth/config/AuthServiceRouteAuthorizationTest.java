package wiki.chiu.micro.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.adapter.out.composite.AuthWrapper;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.application.port.out.VisitRecorder;
import wiki.chiu.micro.auth.application.service.AuthServiceImpl;
import wiki.chiu.micro.auth.token.JwtProperties;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

class AuthServiceRouteAuthorizationTest {

    private AuthWrapper authWrapper;
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
        authService =
            new AuthServiceImpl(authWrapper, mock(VisitRecorder.class), tokens);

        when(authWrapper.getAllRoleAuthorizations()).thenReturn(List.of(role(Set.of())));
    }

    @Test
    void websocketTicketOnlyAuthorizesItsBoundRoom() {
        givenRoutes(List.of(authority("sync_room", "/rooms/**", AuthTypeEnum.NEED_AUTH)));

        String roomTicket = "Bearer " + tokens.issueWebSocketToken(42L, "blog-7");
        String accessToken = "Bearer " + tokens.issueAccessToken(42L);

        AuthorityRouteRpcVo route = authService.authorizeRoute(route("/rooms/blog-7"), roomTicket);
        assertEquals("sync", route.serviceHost());
        assertEquals(Integer.valueOf(8089), route.servicePort());
        assertEquals(42L, route.principal().userId());
        assertThrows(
            MissException.class, () -> authService.authorizeRoute(route("/rooms/blog-8"), roomTicket));
        assertThrows(
            MissException.class, () -> authService.authorizeRoute(route("/rooms/blog-7"), accessToken));
    }

    @Test
    void exactProtectedRouteTakesPrecedenceOverWildcardWhitelist() {
        givenRoutes(
            List.of(
                authority("public_api", "/api/**", AuthTypeEnum.WHITE_LIST),
                authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));

        assertThrows(
            MissException.class, () -> authService.authorizeRoute(route("/api/private"), null));
        assertEquals("service", authService.authorizeRoute(route("/api/public"), null).serviceHost());
        assertEquals(
            AuthPrincipal.anonymous(),
            authService.authorizeRoute(route("/api/public"), null).principal());
        assertThrows(
            MissException.class, () -> authService.authorizeRoute(route("/apix/public"), null));
        verify(authWrapper, never()).getUserAccess(42L);
    }

    @Test
    void whitelistUsesValidatedIdentityWhenTokenIsPresent() {
        givenRoutes(List.of(authority("public_api", "/api/public", AuthTypeEnum.WHITE_LIST)));

        AuthorityRouteRpcVo route =
            authService.authorizeRoute(route("/api/public"), "Bearer " + tokens.issueAccessToken(42L));

        assertEquals(new AuthPrincipal(42L, List.of("user")), route.principal());
        verify(authWrapper, times(1)).getUserAccess(42L);
    }

    @Test
    void whitelistRejectsInvalidTokenInsteadOfDowngradingToAnonymous() {
        givenRoutes(List.of(authority("public_api", "/api/public", AuthTypeEnum.WHITE_LIST)));

        assertThrows(
            MissException.class,
            () -> authService.authorizeRoute(route("/api/public"), "Bearer invalid-token"));
        verify(authWrapper, never()).getUserAccess(42L);
    }

    @Test
    void disabledUserIsRejectedBeforeAuthorityLookup() {
        List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH));
        when(authWrapper.getUserAccess(42L))
            .thenReturn(new UserAccessRpcVo(42L, true, StatusEnum.HIDE.getCode(), List.of(7L)));

        assertThrows(
            MissException.class,
            () ->
                authService.authorizeRoute(
                    route("/api/private"), "Bearer " + tokens.issueAccessToken(42L)));
        verify(authWrapper, never()).getAllRoleAuthorizations();
    }

    @Test
    void invalidAccessTokenIsUnauthenticatedButMissingAuthorityIsForbidden() {
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authWrapper.getAllRoleAuthorizations()).thenReturn(List.of(role(Set.of("other_api"))));

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
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authWrapper.getAllRoleAuthorizations()).thenReturn(List.of(role(Set.of("private_api"))));

        AuthorityRouteRpcVo route =
            authService.authorizeRoute(route("/api/private"), "Bearer " + tokens.issueAccessToken(42L));

        assertEquals("service", route.serviceHost());
        assertEquals(Integer.valueOf(8080), route.servicePort());
        assertEquals(List.of("user"), route.principal().roles());
        verify(authWrapper, times(1)).getUserAccess(42L);
    }

    @Test
    void userLookupFailureIsNotCollapsedIntoForbidden() {
        MissException failure = new MissException(ExceptionMessage.USER_NOT_EXIST);
        when(authWrapper.getUserAccess(42L)).thenThrow(failure);

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

    private void givenRoutes(List<AuthorityRpcVo> routes) {
        lenient().when(authWrapper.getAllSystemAuthorities()).thenReturn(routes);
        lenient()
            .when(authWrapper.getUserAccess(42L))
            .thenReturn(new UserAccessRpcVo(42L, true, StatusEnum.NORMAL.getCode(), List.of(7L)));
    }

    private RoleAuthorizationRpcVo role(Set<String> authorities) {
        return new RoleAuthorizationRpcVo(
            7L, true, "user", StatusEnum.NORMAL.getCode(), authorities, List.of());
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
