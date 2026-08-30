package wiki.chiu.micro.user.adapter.out.redis;

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
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;

@Component
public class RedisRegistrationTokenStore implements RegistrationTokenStore {

  private final StringRedisTemplate redis;
  private final ResourceLoader resources;
  private String consumeForUsernameScript;

  public RedisRegistrationTokenStore(StringRedisTemplate redis, ResourceLoader resources) {
    this.redis = redis;
    this.resources = resources;
  }

  @PostConstruct
  void loadScript() throws IOException {
    consumeForUsernameScript =
        resources
            .getResource(
                ResourceUtils.CLASSPATH_URL_PREFIX + "script/consume-registration-token.lua")
            .getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public String issue(String username) {
    String token = UUID.randomUUID().toString();
    redis
        .opsForValue()
        .set(REGISTER_PREFIX + token, username, Expiration.from(1, TimeUnit.HOURS));
    return token;
  }

  @Override
  public boolean exists(String token) {
    return token != null
        && !token.isBlank()
        && Boolean.TRUE.equals(redis.hasKey(REGISTER_PREFIX + token));
  }

  @Override
  public void consumeForUsername(String token, String username) {
    if (token == null || token.isBlank() || username == null || username.isBlank()) {
      throw new MissException(NO_AUTH);
    }
    Long consumed =
        redis.execute(
            RedisScript.of(consumeForUsernameScript, Long.class),
            List.of(REGISTER_PREFIX + token),
            username);
    if (!Long.valueOf(1L).equals(consumed)) {
      throw new MissException(NO_AUTH);
    }
  }
}
