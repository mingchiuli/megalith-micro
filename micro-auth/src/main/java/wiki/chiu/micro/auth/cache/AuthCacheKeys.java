package wiki.chiu.micro.auth.cache;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.wrapper.AuthWrapper;
import wiki.chiu.micro.cache.utils.CommonCacheKeyGenerator;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;

@Component
public class AuthCacheKeys {

  private final CommonCacheKeyGenerator keyGenerator;

  public AuthCacheKeys(CommonCacheKeyGenerator keyGenerator) {
    this.keyGenerator = keyGenerator;
  }

  public Set<String> from(AuthCacheEvictMessage message) {
    try {
      Set<String> keys = new HashSet<>();
      Method userAccess = AuthWrapper.class.getMethod("getUserAccess", Long.class);
      message.userIds().forEach(id -> keys.add(keyGenerator.generateKey(userAccess, id)));

      if (!message.roleIds().isEmpty()) {
        Method roleAuthorization = AuthWrapper.class.getMethod("getAllRoleAuthorizations");
        keys.add(keyGenerator.generateKey(roleAuthorization));
      }

      if (message.evictMenus()) {
        Method nav = AuthWrapper.class.getMethod("getCurrentUserNav", String.class);
        message.roleCodes().forEach(code -> keys.add(keyGenerator.generateKey(nav, code)));
      }
      if (message.evictRoutes()) {
        Method routes = AuthWrapper.class.getMethod("getAllSystemAuthorities");
        keys.add(keyGenerator.generateKey(routes));
      }
      return keys;
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Auth cache method contract changed", e);
    }
  }
}
