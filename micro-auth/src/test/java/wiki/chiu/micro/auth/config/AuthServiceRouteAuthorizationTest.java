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

import wiki.chiu.micro.auth.adapter.out.token.JwtTokenService;
import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.RouteDecision;
import wiki.chiu.micro.auth.application.model.RouteQuery;
import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.port.out.AuthorizationDirectory;
import wiki.chiu.micro.auth.application.port.out.VisitRecorder;
import wiki.chiu.micro.auth.application.service.AuthServiceImpl;
import wiki.chiu.micro.common.enums.AuthTypeEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.security.AuthPrincipal;

class AuthServiceRouteAuthorizationTest {

    private AuthorizationDirectory authorizationDirectory;
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
                properties.issuer(),
                properties.audience(),
                new JwtTokenService.TokenLifetimes(
                    properties.accessTokenExpire(),
                    properties.refreshTokenExpire(),
                    properties.websocketTokenExpire()));

        authorizationDirectory = mock(AuthorizationDirectory.class);
        authService =
            new AuthServiceImpl(authorizationDirectory, mock(VisitRecorder.class), tokens);

        lenient().when(authorizationDirectory.getAllRoleAuthorizations()).thenReturn(List.of(role(Set.of())));
    }

    @Test
    void websocketTicketOnlyAuthorizesItsBoundRoom() {
        givenRoutes(List.of(authority("sync_room", "/rooms/**", AuthTypeEnum.NEED_AUTH)));

        String roomTicket = "Bearer " + tokens.webSocketTicket(42L, "blog-7");
        String accessToken = "Bearer " + tokens.accessToken(42L);

        RouteDecision route = authService.authorizeRoute(route("/rooms/blog-7"), roomTicket);
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
        verify(authorizationDirectory, never()).getUserAccess(42L);
    }

    @Test
    void whitelistUsesValidatedIdentityWhenTokenIsPresent() {
        givenRoutes(List.of(authority("public_api", "/api/public", AuthTypeEnum.WHITE_LIST)));

        RouteDecision route =
            authService.authorizeRoute(route("/api/public"), "Bearer " + tokens.accessToken(42L));

        assertEquals(new AuthPrincipal(42L, List.of("user")), route.principal());
        verify(authorizationDirectory, times(1)).getUserAccess(42L);
    }

    @Test
    void whitelistRejectsInvalidTokenInsteadOfDowngradingToAnonymous() {
        givenRoutes(List.of(authority("public_api", "/api/public", AuthTypeEnum.WHITE_LIST)));

        assertThrows(
            MissException.class,
            () -> authService.authorizeRoute(route("/api/public"), "Bearer invalid-token"));
        verify(authorizationDirectory, never()).getUserAccess(42L);
    }

    @Test
    void disabledUserIsRejectedBeforeAuthorityLookup() {
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authorizationDirectory.getUserAccess(42L))
            .thenReturn(new UserAccess(42L, true, StatusEnum.HIDE.getCode(), List.of(7L)));

        assertThrows(
            MissException.class,
            () ->
                authService.authorizeRoute(
                    route("/api/private"), "Bearer " + tokens.accessToken(42L)));
        verify(authorizationDirectory, never()).getAllRoleAuthorizations();
    }

    @Test
    void invalidAccessTokenIsUnauthenticatedButMissingAuthorityIsForbidden() {
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authorizationDirectory.getAllRoleAuthorizations())
            .thenReturn(List.of(role(Set.of("other_api"))));

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
                        route("/api/private"), "Bearer " + tokens.accessToken(42L)));
        assertSame(ExceptionMessage.NO_AUTH, missingAuthority.errorCode());
    }

    @Test
    void authorizedRoleReceivesTheResolvedRoute() {
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authorizationDirectory.getAllRoleAuthorizations())
            .thenReturn(List.of(role(Set.of("private_api"))));

        RouteDecision route =
            authService.authorizeRoute(route("/api/private"), "Bearer " + tokens.accessToken(42L));

        assertEquals("service", route.serviceHost());
        assertEquals(Integer.valueOf(8080), route.servicePort());
        assertEquals(List.of("user"), route.principal().roles());
        verify(authorizationDirectory, times(1)).getUserAccess(42L);
    }

    @Test
    void userLookupFailureIsNotCollapsedIntoForbidden() {
        MissException failure = new MissException(ExceptionMessage.USER_NOT_EXIST);
        givenRoutes(List.of(authority("private_api", "/api/private", AuthTypeEnum.NEED_AUTH)));
        when(authorizationDirectory.getUserAccess(42L)).thenThrow(failure);

        MissException actual =
            assertThrows(
                MissException.class,
                () ->
                    authService.authorizeRoute(
                        route("/api/private"), "Bearer " + tokens.accessToken(42L)));

        assertSame(failure, actual);
    }

    private RouteQuery route(String path) {
        return new RouteQuery("GET", path, null);
    }

    private void givenRoutes(List<Authority> routes) {
        lenient().when(authorizationDirectory.getAllSystemAuthorities()).thenReturn(routes);
        lenient()
            .when(authorizationDirectory.getUserAccess(42L))
            .thenReturn(new UserAccess(42L, true, StatusEnum.NORMAL.getCode(), List.of(7L)));
    }

    private RoleAuthorization role(Set<String> authorities) {
        return new RoleAuthorization(7L, true, "user", StatusEnum.NORMAL.getCode(), authorities, List.of());
    }

    private Authority authority(String code, String pattern, AuthTypeEnum type) {
        boolean websocket = pattern.startsWith("/rooms/");
        return new Authority(
            websocket ? 2L : 1L,
            code,
            "GET",
            pattern,
            websocket ? "sync" : "service",
            websocket ? 8089 : 8080,
            type.getCode());
    }
}
