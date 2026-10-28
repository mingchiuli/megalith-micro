package wiki.chiu.micro.auth.application.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

        String[] patterns = pathSegments(routePattern);
        String[] routes = pathSegments(routeMapping);
        return segmentMatch(patterns, routes, 0, 0);
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
                (left, right) -> {
                    int specificity =
                        compareRouteSpecificity(left.routePattern(), right.routePattern());
                    if (specificity != 0) {
                        return specificity;
                    }
                    int pattern = left.routePattern().compareTo(right.routePattern());
                    return pattern != 0 ? pattern : left.code().compareTo(right.code());
                })
            .findFirst();
    }

    private int compareRouteSpecificity(String left, String right) {
        int categoryComparison =
            Integer.compare(routeCategory(right), routeCategory(left));
        if (categoryComparison != 0) {
            return categoryComparison;
        }
        String[] leftSegments = pathSegments(left);
        String[] rightSegments = pathSegments(right);
        for (int index = 0; index < Math.min(leftSegments.length, rightSegments.length); index++) {
            int segmentComparison =
                Integer.compare(
                    segmentSpecificity(rightSegments[index]),
                    segmentSpecificity(leftSegments[index]));
            if (segmentComparison != 0) {
                return segmentComparison;
            }
        }
        if (leftSegments.length != rightSegments.length) {
            return Integer.compare(rightSegments.length, leftSegments.length);
        }
        return Integer.compare(right.length(), left.length());
    }

    private int routeCategory(String pattern) {
        String[] segments = pathSegments(pattern);
        if (Arrays.stream(segments).anyMatch("**"::equals)) {
            return 0;
        }
        if (Arrays.stream(segments).anyMatch("*"::equals)) {
            return 1;
        }
        return 2;
    }

    private int segmentSpecificity(String segment) {
        if ("**".equals(segment)) {
            return 0;
        }
        if ("*".equals(segment)) {
            return 1;
        }
        return 2;
    }

    private boolean segmentMatch(String[] patterns, String[] routes, int patternIndex, int routeIndex) {
        if (patternIndex == patterns.length) {
            return routeIndex == routes.length;
        }
        String pattern = patterns[patternIndex];
        if ("**".equals(pattern)) {
            return patternIndex == patterns.length - 1;
        }
        if (routeIndex == routes.length) {
            return false;
        }
        if ("*".equals(pattern) && routes[routeIndex].isEmpty()) {
            return false;
        }
        if (!"*".equals(pattern) && !Objects.equals(pattern, routes[routeIndex])) {
            return false;
        }
        return segmentMatch(patterns, routes, patternIndex + 1, routeIndex + 1);
    }

    private String[] pathSegments(String path) {
        if (path == null || path.isBlank()) {
            return new String[0];
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        return normalized.split("/", -1);
    }

    private boolean isWebSocketRoute(String routeMapping) {
        return routeMapping.startsWith(WEBSOCKET_ROUTE_PREFIX);
    }
}
