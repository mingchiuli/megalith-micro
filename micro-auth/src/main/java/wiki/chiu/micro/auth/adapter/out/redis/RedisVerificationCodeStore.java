package wiki.chiu.micro.auth.adapter.out.redis;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.auth.application.port.out.VerificationCodeStore;

@Component
public class RedisVerificationCodeStore implements VerificationCodeStore {

  private final RedissonClient redisson;
  private final ResourceLoader resources;
  private String script;

  public RedisVerificationCodeStore(RedissonClient redisson, ResourceLoader resources) {
    this.redisson = redisson;
    this.resources = resources;
  }

  @PostConstruct
  void loadScript() throws IOException {
    script =
        resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/hmset-expire.lua")
            .getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public boolean exists(String key) {
    return redisson.getBucket(key).isExists();
  }

  @Override
  public void save(String key, String code) {
    redisson
        .getScript()
        .eval(
            RScript.Mode.READ_WRITE,
            script,
            RScript.ReturnType.VALUE,
            Collections.singletonList(key),
            "code",
            code,
            "try_count",
            "0",
            "120");
  }
}
