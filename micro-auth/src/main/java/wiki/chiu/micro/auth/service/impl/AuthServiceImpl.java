package wiki.chiu.micro.auth.service.impl;

import static wiki.chiu.micro.common.lang.Const.DAY_VISIT;
import static wiki.chiu.micro.common.lang.Const.MONTH_VISIT;
import static wiki.chiu.micro.common.lang.Const.WEEK_VISIT;
import static wiki.chiu.micro.common.lang.Const.YEAR_VISIT;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
import wiki.chiu.micro.auth.convertor.MenuDisplayDtoConvertor;
import wiki.chiu.micro.auth.convertor.MenuRootVoConvertor;
import wiki.chiu.micro.auth.convertor.MenuWithChildDtoConvertor;
import wiki.chiu.micro.auth.dto.*;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.service.AuthService;
import wiki.chiu.micro.auth.token.JwtTokenService;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;

@Service
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final AuthWrapper authWrapper;

  private final RedissonClient redissonClient;

  private final TaskExecutor taskExecutor;

  private final ResourceLoader resourceLoader;

  private final JwtTokenService jwtTokenService;

  private final UserHttpServiceWrapper userHttpServiceWrapper;

  private String script;

  public AuthServiceImpl(
      AuthWrapper authWrapper,
      RedissonClient redissonClient,
      @Qualifier("commonExecutor") TaskExecutor taskExecutor,
      ResourceLoader resourceLoader,
      JwtTokenService jwtTokenService,
      UserHttpServiceWrapper userHttpServiceWrapper) {
    this.authWrapper = authWrapper;
    this.redissonClient = redissonClient;
    this.taskExecutor = taskExecutor;
    this.resourceLoader = resourceLoader;
    this.jwtTokenService = jwtTokenService;
    this.userHttpServiceWrapper = userHttpServiceWrapper;
  }

  @PostConstruct
  private void init() throws IOException {
    Resource resource =
        resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/multi-pfadd.lua");
    script = resource.getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public MenuWithChildVo getCurrentUserNav(Long userId) {
    List<MenuDto> menus = new ArrayList<>();

    currentRoles(userId).stream().map(authWrapper::getCurrentUserNav).forEach(menus::addAll);

    return MenuRootVoConvertor.convert(
        MenuWithChildDtoConvertor.convert(
            MenuDisplayDtoConvertor.buildTreeMenu(MenuDisplayDtoConvertor.convert(menus))));
  }

  @Override
  public AuthorityRouteRpcVo findRoute(AuthorityRouteReq req) {
    recordIp(req.ipAddr());
    AuthorityRpcVo route =
        matchingAuthority(req.routeMapping(), req.method())
            .orElseThrow(() -> new MissException(ExceptionMessage.NO_AUTH));
    return AuthorityRouteRpcVo.builder()
        .serviceHost(route.serviceHost())
        .servicePort(route.servicePort())
        .build();
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

  @Override
  public AuthRpcVo getAuthVo(String token) {
    if (!StringUtils.hasLength(token)) {
      return AuthRpcVo.builder()
          .userId(0L)
          .roles(Collections.emptyList())
          .authorities(Collections.emptyList())
          .build();
    }

    Jwt jwt;
    try {
      jwt = jwtTokenService.decodeAccessToken(token);
    } catch (JwtException | IllegalArgumentException e) {
      throw new MissException(ExceptionMessage.TOKEN_INVALID.getMsg());
    }
    Long userId = subject(jwt);
    requireActiveUser(userId);
    List<String> roles = currentRoles(userId);
    List<String> authorities = getAuthorities(roles);

    return AuthRpcVo.builder().userId(userId).roles(roles).authorities(authorities).build();
  }

  @Override
  public Boolean routeCheck(AuthorityRouteCheckReq req, String token) {
    Optional<AuthorityRpcVo> matched = matchingAuthority(req.routeMapping(), req.method());
    if (matched.isEmpty()) {
      return false;
    }
    AuthorityRpcVo route = matched.get();
    if (AuthTypeEnum.WHITE_LIST.getCode().equals(route.type())) {
      return true;
    }
    if (!StringUtils.hasLength(token)) {
      return false;
    }

    try {
      Jwt jwt = decodeRouteToken(req.routeMapping(), token);
      Long userId = subject(jwt);
      if (!isActiveUser(userId)) {
        return false;
      }
      if (isWebSocketRoute(req.routeMapping())) {
        return true;
      }
      return currentRoles(userId).stream()
          .map(authWrapper::getAuthoritiesByRoleCode)
          .flatMap(Collection::stream)
          .anyMatch(route.code()::equals);
    } catch (JwtException | IllegalArgumentException | BaseException e) {
      return false;
    }
  }

  private Optional<AuthorityRpcVo> matchingAuthority(String routeMapping, String method) {
    return authWrapper.getAllSystemAuthorities().stream()
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

  private boolean isWebSocketRoute(String routeMapping) {
    return routeMapping.startsWith("/rooms/");
  }

  private List<String> getAuthorities(List<String> roles) {
    return roles.stream()
        .map(authWrapper::getAuthoritiesByRoleCode)
        .flatMap(Collection::stream)
        .distinct()
        .toList();
  }

  private List<String> currentRoles(Long userId) {
    return userHttpServiceWrapper.findRoleCodesByUserId(userId);
  }

  private Long subject(Jwt jwt) {
    try {
      return Long.valueOf(jwt.getSubject());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid JWT subject", e);
    }
  }

  private void requireActiveUser(Long userId) {
    if (!isActiveUser(userId)) {
      throw new MissException(ExceptionMessage.NO_AUTH);
    }
  }

  private boolean isActiveUser(Long userId) {
    return StatusEnum.NORMAL.getCode().equals(userHttpServiceWrapper.findById(userId).status());
  }
}
