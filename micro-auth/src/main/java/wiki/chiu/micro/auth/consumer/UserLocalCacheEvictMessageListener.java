package wiki.chiu.micro.auth.consumer;

import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.cache.AuthCacheKeys;
import wiki.chiu.micro.common.lang.AuthCacheEvictMessage;

@Component
public class UserLocalCacheEvictMessageListener {

  private final Cache<@NonNull String, Object> localCache;
  private final AuthCacheKeys authCacheKeys;

  public UserLocalCacheEvictMessageListener(
      @Qualifier("caffeineCache") Cache<@NonNull String, Object> localCache,
      AuthCacheKeys authCacheKeys) {
    this.localCache = localCache;
    this.authCacheKeys = authCacheKeys;
  }

  public void handleMessage(AuthCacheEvictMessage message) {
    localCache.invalidateAll(authCacheKeys.from(message));
  }

  @EventListener(ApplicationReadyEvent.class)
  public void clearOnStartup() {
    localCache.invalidateAll();
  }
}
