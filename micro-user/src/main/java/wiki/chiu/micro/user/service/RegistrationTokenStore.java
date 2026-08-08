package wiki.chiu.micro.user.service;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.exception.MissException;

@Component
public class RegistrationTokenStore {

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

  public String username(String token) {
    return redisTemplate.opsForValue().get(REGISTER_PREFIX + token);
  }

  public void consume(String token) {
    redisTemplate.delete(REGISTER_PREFIX + token);
  }
}
