package wiki.chiu.micro.auth.service.impl;

import static wiki.chiu.micro.common.lang.Const.DAY_VISIT;
import static wiki.chiu.micro.common.lang.Const.MONTH_VISIT;
import static wiki.chiu.micro.common.lang.Const.WEEK_VISIT;
import static wiki.chiu.micro.common.lang.Const.YEAR_VISIT;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.convertor.MenuDisplayDtoConvertor;
import wiki.chiu.micro.auth.convertor.MenuRootVoConvertor;
import wiki.chiu.micro.auth.convertor.MenuWithChildDtoConvertor;
import wiki.chiu.micro.auth.dto.*;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final AuthWrapper authWrapper;

  private final RedissonClient redissonClient;

  private final TaskExecutor taskExecutor;

  private final ResourceLoader resourceLoader;

  private final JwtTokenService jwtTokenService;

  private String script;

  public AuthServiceImpl(
      AuthWrapper authWrapper,
      RedissonClient redissonClient,
      @Qualifier("commonExecutor") TaskExecutor taskExecutor,
      ResourceLoader resourceLoader,
      JwtTokenService jwtTokenService) {
    this.authWrapper = authWrapper;
    this.redissonClient = redissonClient;
    this.taskExecutor = taskExecutor;
    this.resourceLoader = resourceLoader;
    this.jwtTokenService = jwtTokenService;
  }

  @PostConstruct
  private void init() throws IOException {
    Resource resource =
        resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/multi-pfadd.lua");
    script = resource.getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public MenuWithChildVo getCurrentUserNav(List<String> roles) {
    List<MenuDto> menus = new ArrayList<>();

    roles.stream().map(authWrapper::getCurrentUserNav).forEach(menus::addAll);

    return MenuRootVoConvertor.convert(
        MenuWithChildDtoConvertor.convert(
            MenuDisplayDtoConvertor.buildTreeMenu(MenuDisplayDtoConvertor.convert(menus))));
  }

  @Override
  public AuthorityRouteRpcVo authorizeRoute(AuthorityRouteReq req, String token) {
    Long userId = resolveUserId(req.routeMapping(), token);
    List<AuthorityRpcVo> routes = authWrapper.getAllSystemAuthorities();
    UserAccessRpcVo access = userId == null ? null : authWrapper.getUserAccess(userId);
    AuthorityRpcVo route =
        matchingAuthority(routes, req.routeMapping(), req.method())
            .orElseThrow(() -> new MissException(ExceptionMessage.NO_AUTH));
    AuthPrincipal principal = authorizePrincipal(req.routeMapping(), route, userId, access);
    recordIp(req.ipAddr());
    return AuthorityRouteRpcVo.builder()
        .serviceHost(route.serviceHost())
        .servicePort(route.servicePort())
        .principal(principal)
        .build();
  }

  private List<RoleAuthorizationRpcVo> roleAuthorizations(List<Long> roleIds) {
    List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
    if (distinctRoleIds.isEmpty()) {
      return List.of();
    }
    Map<Long, RoleAuthorizationRpcVo> byId =
        authWrapper.getAllRoleAuthorizations().stream()
            .collect(Collectors.toMap(RoleAuthorizationRpcVo::roleId, Function.identity()));
    return distinctRoleIds.stream()
        .map(roleId -> byId.getOrDefault(roleId, RoleAuthorizationRpcVo.missing(roleId)))
        .toList();
  }

  private void recordIp(String ipAddr) {
    if (StringUtils.hasLength(ipAddr)) {
      log.info("Record visit IP: {}", ipAddr);
      // Use Lua script to atomically increment visit counts for different time periods
      taskExecutor.execute(
          () ->
              redissonClient
                  .getScript()
                  .eval(
                      Mode.READ_WRITE,
                      script,
                      ReturnType.VALUE,
                      List.of(DAY_VISIT, WEEK_VISIT, MONTH_VISIT, YEAR_VISIT),
                      ipAddr,
                      ipAddr,
                      ipAddr,
                      ipAddr));
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
      String routeMapping, AuthorityRpcVo route, Long userId, UserAccessRpcVo access) {
    if (userId == null) {
      if (AuthTypeEnum.WHITE_LIST.getCode().equals(route.type())) {
        return AuthPrincipal.anonymous();
      }
      throw new MissException(ExceptionMessage.TOKEN_INVALID);
    }

    if (access == null
        || !access.exists()
        || !StatusEnum.NORMAL.getCode().equals(access.status())) {
      throw new MissException(ExceptionMessage.NO_AUTH);
    }
    List<RoleAuthorizationRpcVo> authorizations =
        roleAuthorizations(access.roleIds()).stream()
            .filter(RoleAuthorizationRpcVo::exists)
            .filter(item -> StatusEnum.NORMAL.getCode().equals(item.status()))
            .toList();
    List<String> roles =
        authorizations.stream().map(RoleAuthorizationRpcVo::code).distinct().toList();
    List<wiki.chiu.micro.common.lang.DataPermissionEnum> dataPermissions =
        authorizations.stream()
            .map(RoleAuthorizationRpcVo::dataPermissions)
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
            .map(RoleAuthorizationRpcVo::authorityCodes)
            .flatMap(Collection::stream)
            .anyMatch(route.code()::equals);
    if (!authorized) {
      throw new MissException(ExceptionMessage.NO_AUTH);
    }
    return principal;
  }

  private Optional<AuthorityRpcVo> matchingAuthority(
      List<AuthorityRpcVo> routes, String routeMapping, String method) {
    return routes.stream()
        .filter(
            authority ->
                routeMatch(authority.routePattern(), authority.methodType(), routeMapping, method))
        .sorted(
            Comparator.comparingInt(
                    (AuthorityRpcVo authority) -> routeSpecificity(authority.routePattern()))
                .reversed()
                .thenComparing(AuthorityRpcVo::routePattern)
                .thenComparing(AuthorityRpcVo::code))
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

  private Jwt decodeRouteToken(String routeMapping, String token) {
    if (isWebSocketRoute(routeMapping)) {
      Jwt jwt = jwtTokenService.decodeWebSocketToken(token);
      String roomId = routeMapping.substring("/rooms/".length());
      if (roomId.isEmpty() || !roomId.equals(jwt.getClaimAsString("room_id"))) {
        throw new IllegalArgumentException("WebSocket ticket does not match the room");
      }
      return jwt;
    }
    return jwtTokenService.decodeAccessToken(token);
  }

  private Long resolveUserId(String routeMapping, String token) {
    if (!StringUtils.hasLength(token)) {
      return null;
    }
    try {
      return subject(decodeRouteToken(routeMapping, token));
    } catch (JwtException | IllegalArgumentException e) {
      throw new MissException(ExceptionMessage.TOKEN_INVALID);
    }
  }

  private boolean isWebSocketRoute(String routeMapping) {
    return routeMapping.startsWith("/rooms/");
  }

  private Long subject(Jwt jwt) {
    try {
      return Long.valueOf(jwt.getSubject());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid JWT subject", e);
    }
  }
}
