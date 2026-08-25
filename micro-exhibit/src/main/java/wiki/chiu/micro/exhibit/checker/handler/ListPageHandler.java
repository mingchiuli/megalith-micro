package wiki.chiu.micro.exhibit.checker.handler;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;

@Component
public class ListPageHandler {

  private final RedissonClient redissonClient;

  public ListPageHandler(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  public void check(Integer currentPage) {
    RBitSet bitSet = redissonClient.getBitSet(Const.BLOOM_FILTER_PAGE);
    if (!bitSet.isExists()) {
      return;
    }
    boolean bit = bitSet.get(currentPage);
    if (!bit) {
      throw new MissException(NO_FOUND.getMsg() + currentPage + " page");
    }
  }
}
