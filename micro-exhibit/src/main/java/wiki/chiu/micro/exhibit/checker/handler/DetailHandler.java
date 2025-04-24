package wiki.chiu.micro.exhibit.checker.handler;

import wiki.chiu.micro.cache.handler.CheckerHandler;
import wiki.chiu.micro.common.exception.MissException;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import static wiki.chiu.micro.common.lang.Const.BLOOM_FILTER_BLOG;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Component
public class DetailHandler extends CheckerHandler {

    private final RedissonClient redissonClient;

    public DetailHandler(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void handle(Object[] args) {
        Long blogId = (Long) args[0];
        boolean bit = redissonClient.getBitSet(BLOOM_FILTER_BLOG).get(blogId);
        if (!bit) {
            throw new MissException(NO_FOUND.getMsg() + blogId + " blog");
        }
    }
}
