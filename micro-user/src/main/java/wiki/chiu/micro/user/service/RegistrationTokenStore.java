package wiki.chiu.micro.user.service;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.common.exception.MissException;

@Component
public class RegistrationTokenStore {

  private final StringRedisTemplate redisTemplate;
  private final ResourceLoader resourceLoader;
  private String consumeForUsernameScript;

  public RegistrationTokenStore(StringRedisTemplate redisTemplate, ResourceLoader resourceLoader) {
    this.redisTemplate = redisTemplate;
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  void loadScript() throws IOException {
    consumeForUsernameScript =
        resourceLoader
            .getResource(
                ResourceUtils.CLASSPATH_URL_PREFIX + "script/consume-registration-token.lua")
            .getContentAsString(StandardCharsets.UTF_8);
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
        redisTemplate.execute(
            RedisScript.of(consumeForUsernameScript, Long.class),
            List.of(REGISTER_PREFIX + token),
            username);
    if (!Long.valueOf(1L).equals(consumed)) {
      throw new MissException(NO_AUTH);
    }
  }
}
