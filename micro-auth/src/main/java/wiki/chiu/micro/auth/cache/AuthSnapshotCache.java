package wiki.chiu.micro.auth.cache;

import com.github.benmanes.caffeine.cache.Cache;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

@Component
public class AuthSnapshotCache {

  private static final Logger log = LoggerFactory.getLogger(AuthSnapshotCache.class);
  private static final Duration REMOTE_TTL = Duration.ofMinutes(30);
  private static final Method ROUTES_METHOD = method("getAllSystemAuthorities");
  private static final Method USER_ACCESS_METHOD = method("getUserAccess", Long.class);
  private static final Method ROLE_AUTHORIZATION_METHOD =
      method("getRoleAuthorization", Long.class);

  private final RedissonClient redissonClient;
  private final Cache<@NonNull String, Object> localCache;
  private final CommonCacheKeyGenerator keyGenerator;
  private final JsonMapper jsonMapper;
  private final UserHttpServiceWrapper users;
  private final JavaType routesType;

  public AuthSnapshotCache(
      RedissonClient redissonClient,
      @Qualifier("caffeineCache") Cache<@NonNull String, Object> localCache,
      CommonCacheKeyGenerator keyGenerator,
      JsonMapper jsonMapper,
      UserHttpServiceWrapper users) {
    this.redissonClient = redissonClient;
    this.localCache = localCache;
    this.keyGenerator = keyGenerator;
    this.jsonMapper = jsonMapper;
    this.users = users;
    this.routesType =
        jsonMapper.getTypeFactory().constructCollectionType(List.class, AuthorityRpcVo.class);
  }

  public InitialSnapshots loadInitial(Long userId) {
    String routesKey = keyGenerator.generateKey(ROUTES_METHOD);
    String accessKey = userId == null ? null : keyGenerator.generateKey(USER_ACCESS_METHOD, userId);
    List<String> missingKeys = new ArrayList<>(2);

    List<AuthorityRpcVo> routes = local(routesKey);
    if (routes == null) {
      missingKeys.add(routesKey);
    }
    UserAccessRpcVo access = accessKey == null ? null : local(accessKey);
    if (accessKey != null && access == null) {
      missingKeys.add(accessKey);
    }

    Map<String, String> remote = readRemote(missingKeys);
    if (routes == null && remote.containsKey(routesKey)) {
      routes = deserialize(routesKey, remote.get(routesKey), routesType);
    }
    if (accessKey != null && access == null && remote.containsKey(accessKey)) {
      access = deserialize(accessKey, remote.get(accessKey), UserAccessRpcVo.class);
    }

    Map<String, Object> fetched = new LinkedHashMap<>();
    if (routes == null) {
      routes = List.copyOf(users.getSystemAuthorities());
      fetched.put(routesKey, routes);
    }
    if (accessKey != null && access == null) {
      access = users.findUserAccess(userId);
      fetched.put(accessKey, access);
    }
    cacheFetched(fetched);
    return new InitialSnapshots(routes, access);
  }

  public List<RoleAuthorizationRpcVo> loadRoleAuthorizations(List<Long> roleIds) {
    List<Long> distinctRoleIds = roleIds.stream().distinct().toList();
    if (distinctRoleIds.isEmpty()) {
      return List.of();
    }

    Map<Long, String> keys =
        distinctRoleIds.stream()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    roleId -> keyGenerator.generateKey(ROLE_AUTHORIZATION_METHOD, roleId),
                    (_, right) -> right,
                    LinkedHashMap::new));
    Map<Long, RoleAuthorizationRpcVo> snapshots = new LinkedHashMap<>();
    List<String> missingKeys = new ArrayList<>();
    keys.forEach(
        (roleId, key) -> {
          RoleAuthorizationRpcVo cached = local(key);
          if (cached == null) {
            missingKeys.add(key);
          } else {
            snapshots.put(roleId, cached);
          }
        });

    Map<String, String> remote = readRemote(missingKeys);
    keys.forEach(
        (roleId, key) -> {
          if (!snapshots.containsKey(roleId) && remote.containsKey(key)) {
            snapshots.put(roleId, deserialize(key, remote.get(key), RoleAuthorizationRpcVo.class));
          }
        });

    List<Long> uncachedRoleIds =
        distinctRoleIds.stream().filter(roleId -> !snapshots.containsKey(roleId)).toList();
    if (!uncachedRoleIds.isEmpty()) {
      Map<Long, RoleAuthorizationRpcVo> fetchedById =
          users.findRoleAuthorizations(uncachedRoleIds).stream()
              .collect(Collectors.toMap(RoleAuthorizationRpcVo::roleId, Function.identity()));
      Map<String, Object> fetched = new LinkedHashMap<>();
      uncachedRoleIds.forEach(
          roleId -> {
            RoleAuthorizationRpcVo snapshot =
                fetchedById.getOrDefault(roleId, RoleAuthorizationRpcVo.missing(roleId));
            snapshots.put(roleId, snapshot);
            fetched.put(keys.get(roleId), snapshot);
          });
      cacheFetched(fetched);
    }
    return distinctRoleIds.stream().map(snapshots::get).filter(Objects::nonNull).toList();
  }

  private Map<String, String> readRemote(List<String> keys) {
    if (keys.isEmpty()) {
      return Map.of();
    }
    try {
      return redissonClient.getBuckets().get(keys.toArray(String[]::new));
    } catch (RuntimeException failure) {
      log.warn("Auth L2 cache read failed; falling back to user service", failure);
      return Map.of();
    }
  }

  private void cacheFetched(Map<String, Object> fetched) {
    if (fetched.isEmpty()) {
      return;
    }
    fetched.forEach(localCache::put);
    try {
      RBatch batch = redissonClient.createBatch();
      fetched.forEach(
          (key, value) ->
              batch
                  .<String>getBucket(key)
                  .setAsync(jsonMapper.writeValueAsString(value), REMOTE_TTL));
      batch.execute();
    } catch (RuntimeException failure) {
      log.warn("Auth L2 cache write failed; retaining the local snapshot", failure);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T local(String key) {
    return (T) localCache.getIfPresent(key);
  }

  private <T> T deserialize(String key, String value, Class<T> type) {
    T result = jsonMapper.readValue(value, type);
    localCache.put(key, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T> T deserialize(String key, String value, JavaType type) {
    T result = (T) jsonMapper.readValue(value, type);
    localCache.put(key, result);
    return result;
  }

  private static Method method(String name, Class<?>... parameterTypes) {
    try {
      return AuthWrapper.class.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public record InitialSnapshots(List<AuthorityRpcVo> routes, UserAccessRpcVo userAccess) {}
}
