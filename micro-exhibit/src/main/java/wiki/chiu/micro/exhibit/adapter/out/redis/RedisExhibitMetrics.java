package wiki.chiu.micro.exhibit.adapter.out.redis;

import static wiki.chiu.micro.common.lang.Const.DAY_VISIT;
import static wiki.chiu.micro.common.lang.Const.HOT_READ;
import static wiki.chiu.micro.common.lang.Const.MONTH_VISIT;
import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;
import static wiki.chiu.micro.common.lang.Const.WEEK_VISIT;
import static wiki.chiu.micro.common.lang.Const.YEAR_VISIT;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import wiki.chiu.micro.exhibit.application.model.BlogScore;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;

@Component
public class RedisExhibitMetrics implements ExhibitMetrics {

  private final RedissonClient redisson;
  private final ResourceLoader resources;
  private String visitScript;
  private String consumeReadTokenScript;

  public RedisExhibitMetrics(RedissonClient redisson, ResourceLoader resources) {
    this.redisson = redisson;
    this.resources = resources;
  }

  @PostConstruct
  void loadScripts() throws IOException {
    visitScript = readScript("multi-pfcount.lua");
    consumeReadTokenScript = readScript("compare-delete.lua");
  }

  @Override
  public boolean consumeReadToken(Long blogId, String token) {
    Boolean consumed =
        redisson
            .getScript()
            .eval(
                Mode.READ_WRITE,
                consumeReadTokenScript,
                ReturnType.BOOLEAN,
                List.of(READ_TOKEN + blogId),
                token);
    return Boolean.TRUE.equals(consumed);
  }

  @Override
  public List<Long> visitCounts() {
    return redisson
        .getScript()
        .eval(
            Mode.READ_WRITE,
            visitScript,
            ReturnType.LIST,
            List.of(DAY_VISIT, WEEK_VISIT, MONTH_VISIT, YEAR_VISIT));
  }

  @Override
  public List<BlogScore> topReadBlogs(int limit) {
    return redisson.<String>getScoredSortedSet(HOT_READ).entryRangeReversed(0, limit - 1).stream()
        .map(
            entry ->
                new BlogScore(
                    Long.valueOf(entry.getValue()),
                    entry.getScore() == null ? 0L : entry.getScore().longValue()))
        .toList();
  }

  private String readScript(String name) throws IOException {
    return resources
        .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/" + name)
        .getContentAsString(StandardCharsets.UTF_8);
  }
}
