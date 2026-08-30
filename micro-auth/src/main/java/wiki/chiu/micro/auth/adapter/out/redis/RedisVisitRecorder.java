package wiki.chiu.micro.auth.adapter.out.redis;

import static wiki.chiu.micro.common.lang.Const.DAY_VISIT;
import static wiki.chiu.micro.common.lang.Const.MONTH_VISIT;
import static wiki.chiu.micro.common.lang.Const.WEEK_VISIT;
import static wiki.chiu.micro.common.lang.Const.YEAR_VISIT;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.auth.application.port.out.VisitRecorder;

@Component
public class RedisVisitRecorder implements VisitRecorder {

  private final RedissonClient redisson;
  private final TaskExecutor executor;
  private final ResourceLoader resources;
  private String script;

  public RedisVisitRecorder(
      RedissonClient redisson,
      @Qualifier("commonExecutor") TaskExecutor executor,
      ResourceLoader resources) {
    this.redisson = redisson;
    this.executor = executor;
    this.resources = resources;
  }

  @PostConstruct
  void loadScript() throws IOException {
    script =
        resources
            .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/multi-pfadd.lua")
            .getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public void record(String ipAddress) {
    executor.execute(
        () ->
            redisson
                .getScript()
                .eval(
                    Mode.READ_WRITE,
                    script,
                    ReturnType.VALUE,
                    List.of(DAY_VISIT, WEEK_VISIT, MONTH_VISIT, YEAR_VISIT),
                    ipAddress,
                    ipAddress,
                    ipAddress,
                    ipAddress));
  }
}
