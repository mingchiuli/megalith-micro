package wiki.chiu.micro.exhibit.checker.handler;

import org.redisson.api.RBitSet;
import wiki.chiu.micro.cache.handler.CheckerHandler;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Component
public class ListPageHandler extends CheckerHandler {

    private final RedissonClient redissonClient;

    public ListPageHandler(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void handle(Object[] args) {

        RBitSet bitSet = redissonClient.getBitSet(Const.BLOOM_FILTER_PAGE);
        if (!bitSet.isExists()) {
            return;
        }

        Integer currentPage = (Integer) args[0];
        boolean bit = bitSet.get(currentPage);
        if (!bit) {
            throw new MissException(NO_FOUND.getMsg() + currentPage + " page");
        }
    }
}