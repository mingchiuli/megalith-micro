package wiki.chiu.micro.user.service;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.exception.MissException;

@Component
public class RegistrationTokenStore {

  private static final DefaultRedisScript<Long> CONSUME_FOR_USERNAME =
      new DefaultRedisScript<>(
          """
          local username = redis.call('GET', KEYS[1])
          if not username or username ~= ARGV[1] then
            return 0
          end
          return redis.call('DEL', KEYS[1])
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  public RegistrationTokenStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public String issue(String username) {
    String token = UUID.randomUUID().toString();
    redisTemplate
        .opsForValue()
        .set(REGISTER_PREFIX + token, username, Expiration.from(1, TimeUnit.HOURS));
    return token;
  }

  public boolean exists(String token) {
    return token != null
        && !token.isBlank()
        && Boolean.TRUE.equals(redisTemplate.hasKey(REGISTER_PREFIX + token));
  }

  public void requireValid(String token) {
    if (!exists(token)) {
      throw new MissException(NO_AUTH);
    }
  }

  public void consumeForUsername(String token, String username) {
    if (token == null || token.isBlank() || username == null || username.isBlank()) {
      throw new MissException(NO_AUTH);
    }
    Long consumed =
        redisTemplate.execute(CONSUME_FOR_USERNAME, List.of(REGISTER_PREFIX + token), username);
    if (!Long.valueOf(1L).equals(consumed)) {
      throw new MissException(NO_AUTH);
    }
  }
}
