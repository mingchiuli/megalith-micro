package wiki.chiu.micro.auth.consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;

class UserLocalCacheEvictMessageListenerTest {

  @Test
  void invalidatesKeysFromTheOutboxMessage() {
    @SuppressWarnings("unchecked")
    Cache<String, Object> localCache = Mockito.mock(Cache.class);
    AuthCacheKeys keys = Mockito.mock(AuthCacheKeys.class);
    UserLocalCacheEvictMessageListener listener =
        new UserLocalCacheEvictMessageListener(localCache, keys);
    AuthCacheEvictMessage event =
        new AuthCacheEvictMessage("event-1", List.of(7L), List.of(), List.of(), false, false);
    Set<String> cacheKeys = Set.of("authz:user:7");
    when(keys.from(event)).thenReturn(cacheKeys);

    listener.handleMessage(event);

    verify(localCache).invalidateAll(cacheKeys);
  }
}
