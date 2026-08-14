package wiki.chiu.micro.auth.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;

class AuthSnapshotCacheTest {

  @Test
  void loadsRoutesAndUserThenAllRolesInTwoRedisBatches() throws Exception {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    CommonCacheKeyGenerator keys = new CommonCacheKeyGenerator(jsonMapper);
    RedissonClient redisson = mock(RedissonClient.class);
    @SuppressWarnings("unchecked")
    RBuckets buckets = mock(RBuckets.class);
    UserHttpServiceWrapper users = mock(UserHttpServiceWrapper.class);
    when(redisson.getBuckets()).thenReturn(buckets);

    String routesKey = keys.generateKey(AuthWrapper.class.getMethod("getAllSystemAuthorities"));
    String accessKey =
        keys.generateKey(AuthWrapper.class.getMethod("getUserAccess", Long.class), 42L);
    String role7Key =
        keys.generateKey(AuthWrapper.class.getMethod("getRoleAuthorization", Long.class), 7L);
    String role8Key =
        keys.generateKey(AuthWrapper.class.getMethod("getRoleAuthorization", Long.class), 8L);
    List<AuthorityRpcVo> routes =
        List.of(
            AuthorityRpcVo.builder()
                .code("blog_read")
                .methodType("GET")
                .routePattern("/blog/*")
                .serviceHost("blog")
                .servicePort(8082)
                .type(1)
                .build());
    UserAccessRpcVo access =
        new UserAccessRpcVo(42L, true, StatusEnum.NORMAL.getCode(), List.of(7L, 8L));
    RoleAuthorizationRpcVo role7 = role(7L, "user");
    RoleAuthorizationRpcVo role8 = role(8L, "editor");
    when(buckets.get(any(String[].class)))
        .thenReturn(
            Map.of(
                routesKey,
                jsonMapper.writeValueAsString(routes),
                accessKey,
                jsonMapper.writeValueAsString(access)),
            Map.of(
                role7Key,
                jsonMapper.writeValueAsString(role7),
                role8Key,
                jsonMapper.writeValueAsString(role8)));

    AuthSnapshotCache cache =
        new AuthSnapshotCache(redisson, Caffeine.newBuilder().build(), keys, jsonMapper, users);

    var initial = cache.loadInitial(42L);
    var authorizations = cache.loadRoleAuthorizations(initial.userAccess().roleIds());

    assertEquals(routes, initial.routes());
    assertEquals(access, initial.userAccess());
    assertEquals(List.of(role7, role8), authorizations);
    verify(buckets, org.mockito.Mockito.times(2)).get(any(String[].class));
    verify(users, never()).getSystemAuthorities();
    verify(users, never()).findUserAccess(42L);
    verify(users, never()).findRoleAuthorizations(List.of(7L, 8L));
  }

  private RoleAuthorizationRpcVo role(Long id, String code) {
    return new RoleAuthorizationRpcVo(
        id, true, code, StatusEnum.NORMAL.getCode(), Set.of("blog_read"), List.of());
  }
}
