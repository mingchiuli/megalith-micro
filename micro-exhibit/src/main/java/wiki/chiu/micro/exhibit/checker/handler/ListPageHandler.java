package wiki.chiu.micro.exhibit.checker.handler;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;

import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import wiki.chiu.micro.cache.handler.CheckerHandler;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.web.ValidatedRequest;

@Component
public class ListPageHandler extends CheckerHandler {

  private final RedissonClient redissonClient;
  private final ValidatedRequest v;

  public ListPageHandler(RedissonClient redissonClient, ValidatedRequest v) {
    this.redissonClient = redissonClient;
    this.v = v;
  }

  @Override
  public void handle(Object[] args) {

    RBitSet bitSet = redissonClient.getBitSet(Const.BLOOM_FILTER_PAGE);
    if (!bitSet.isExists()) {
      return;
    }

    Integer currentPage =
        v.positive(
            pathVariable((ServerRequest) args[0], "currentPage", Integer::valueOf), "currentPage");
    boolean bit = bitSet.get(currentPage);
    if (!bit) {
      throw new MissException(NO_FOUND.getMsg() + currentPage + " page");
    }
  }
}
