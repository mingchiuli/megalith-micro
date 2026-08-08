package wiki.chiu.micro.user.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import wiki.chiu.micro.common.exception.MissException;

class RegistrationTokenStoreTest {

  private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
  private final RegistrationTokenStore tokens = new RegistrationTokenStore(redis);

  @Test
  @SuppressWarnings("unchecked")
  void consumesMatchingUsernameWithOneRedisScript() {
    when(redis.execute(any(RedisScript.class), eq(List.of("register_prefix:token")), eq("alice")))
        .thenReturn(1L);

    tokens.consumeForUsername("token", "alice");

    verify(redis)
        .execute(any(RedisScript.class), eq(List.of("register_prefix:token")), eq("alice"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void rejectsMissingOrMismatchedToken() {
    when(redis.execute(any(RedisScript.class), eq(List.of("register_prefix:token")), eq("mallory")))
        .thenReturn(0L);

    assertThrows(MissException.class, () -> tokens.consumeForUsername("token", "mallory"));
    assertThrows(MissException.class, () -> tokens.consumeForUsername("", "alice"));
  }
}
