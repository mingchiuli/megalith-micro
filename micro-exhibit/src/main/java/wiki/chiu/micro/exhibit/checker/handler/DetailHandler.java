package wiki.chiu.micro.exhibit.checker.handler;

import static wiki.chiu.micro.common.lang.Const.BLOOM_FILTER_BLOG;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.exception.MissException;

@Component
public class DetailHandler {

  private final RedissonClient redissonClient;

  public DetailHandler(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }

  public void check(Long blogId) {
    RBitSet bitSet = redissonClient.getBitSet(BLOOM_FILTER_BLOG);
    boolean exists = bitSet.isExists();
    if (!exists) {
      return;
    }
    boolean bit = bitSet.get(blogId);
    if (!bit) {
      throw new MissException(NO_FOUND.getMsg() + blogId + " blog");
    }
  }
}
