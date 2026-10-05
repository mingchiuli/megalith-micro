package wiki.chiu.micro.auth.application.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wiki.chiu.micro.auth.application.model.Authority;
import wiki.chiu.micro.auth.application.model.Menu;
import wiki.chiu.micro.auth.application.model.MenuDisplay;
import wiki.chiu.micro.auth.application.model.RoleAuthorization;
import wiki.chiu.micro.auth.application.model.RouteDecision;
import wiki.chiu.micro.auth.application.model.RouteQuery;
import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.port.in.AuthService;
import wiki.chiu.micro.auth.application.port.out.AuthorizationDirectory;
import wiki.chiu.micro.auth.application.port.out.RouteTokenReader;
import wiki.chiu.micro.auth.application.port.out.VisitRecorder;
import wiki.chiu.micro.common.enums.AuthTypeEnum;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.security.AuthPrincipal;

public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String WEBSOCKET_ROUTE_PREFIX = "/rooms/";

    private final AuthorizationDirectory authorizationDirectory;

    private final VisitRecorder visits;

    private final RouteTokenReader routeTokens;

    public AuthServiceImpl(
        AuthorizationDirectory authorizationDirectory,
        VisitRecorder visits,
        RouteTokenReader routeTokens) {
        this.authorizationDirectory = authorizationDirectory;
        this.visits = visits;
        this.routeTokens = routeTokens;
    }

    @Override
    public List<MenuDisplay> getCurrentUserNav(List<String> roles) {
        List<Menu> menus = new ArrayList<>();

        roles.stream().map(authorizationDirectory::getCurrentUserNav).forEach(menus::addAll);

        return MenuTree.build(menus);
    }

    @Override
    public RouteDecision authorizeRoute(RouteQuery query, String token) {
        Long userId = routeTokens.resolveUserId(query.routeMapping(), token);
        List<Authority> routes = authorizationDirectory.getAllSystemAuthorities();
        UserAccess access = userId == null ? null : authorizationDirectory.getUserAccess(userId);
        Authority route =
            matchingAuthority(routes, query.routeMapping(), query.method())
                .orElseThrow(() -> new MissException(ExceptionMessage.NO_AUTH));
        AuthPrincipal principal = authorizePrincipal(query.routeMapping(), route, userId, access);
        recordIp(query.ipAddr());
        return new RouteDecision(route.serviceHost(), route.servicePort(), principal);
    }

    private List<RoleAuthorization> roleAuthorizations(List<Long> roleIds) {
        List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
        if (distinctRoleIds.isEmpty()) {
            return List.of();
        }
        Map<Long, RoleAuthorization> byId =
            authorizationDirectory.getAllRoleAuthorizations().stream()
                .collect(Collectors.toMap(RoleAuthorization::roleId, Function.identity()));
        return distinctRoleIds.stream()
            .map(roleId -> byId.getOrDefault(roleId, RoleAuthorization.missing(roleId)))
            .toList();
    }

    private void recordIp(String ipAddr) {
        if (ipAddr != null && !ipAddr.isEmpty()) {
            log.info("Record visit IP: {}", ipAddr);
            visits.record(ipAddr);
        }
    }

    private boolean routeMatch(
        String routePattern, String targetMethod, String routeMapping, String method) {
        if (!Objects.equals(targetMethod, method)) {
            return false;
        }

        if (Objects.equals(routePattern, routeMapping)) {
            return true;
        }

        if (routePattern.endsWith("/**")) {
            String prefix = routePattern.replace("/**", "");

            return routeMapping.equals(prefix) || routeMapping.startsWith(prefix + "/");
        }

        if (routePattern.endsWith("/*")) {
            String prefix = routePattern.replace("/*", "");

            if (!routeMapping.startsWith(prefix + "/")) {
                return false;
            }
            String remaining = routeMapping.substring(prefix.length() + 1);
            return !remaining.isEmpty() && !remaining.contains("/");
        }

        return false;
    }

    private AuthPrincipal authorizePrincipal(
        String routeMapping, Authority route, Long userId, UserAccess access) {
        if (userId == null) {
            if (AuthTypeEnum.WHITE_LIST.getCode().equals(route.type())) {
                return AuthPrincipal.anonymous();
            }
            throw new MissException(ExceptionMessage.TOKEN_INVALID);
        }

        if (access == null || !access.exists() || !StatusEnum.NORMAL.getCode().equals(access.status())) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }
        List<RoleAuthorization> authorizations =
            roleAuthorizations(access.roleIds()).stream()
                .filter(RoleAuthorization::exists)
                .filter(item -> StatusEnum.NORMAL.getCode().equals(item.status()))
                .toList();
        List<String> roles =
            authorizations.stream().map(RoleAuthorization::code).distinct().toList();
        List<DataPermissionEnum> dataPermissions =
            authorizations.stream()
                .map(RoleAuthorization::dataPermissions)
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .toList();
        AuthPrincipal principal = new AuthPrincipal(access.userId(), roles, dataPermissions);
        if (isWebSocketRoute(routeMapping)) {
            return principal;
        }
        if (AuthTypeEnum.WHITE_LIST.getCode().equals(route.type())) {
            return principal;
        }
        boolean authorized =
            authorizations.stream()
                .map(RoleAuthorization::authorityCodes)
                .flatMap(Collection::stream)
                .anyMatch(route.code()::equals);
        if (!authorized) {
            throw new MissException(ExceptionMessage.NO_AUTH);
        }
        return principal;
    }

    private Optional<Authority> matchingAuthority(
        List<Authority> routes, String routeMapping, String method) {
        return routes.stream()
            .filter(
                authority ->
                    routeMatch(authority.routePattern(), authority.methodType(), routeMapping, method))
            .sorted(
                Comparator.comparingInt(
                        (Authority authority) -> routeSpecificity(authority.routePattern()))
                    .reversed()
                    .thenComparing(Authority::routePattern)
                    .thenComparing(Authority::code))
            .findFirst();
    }

    private int routeSpecificity(String pattern) {
        if (pattern.endsWith("/**")) {
            return 1_000 + pattern.length();
        }
        if (pattern.endsWith("/*")) {
            return 2_000 + pattern.length();
        }
        return 3_000 + pattern.length();
    }

    private boolean isWebSocketRoute(String routeMapping) {
        return routeMapping.startsWith(WEBSOCKET_ROUTE_PREFIX);
    }
}
