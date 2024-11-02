package wiki.chiu.micro.exhibit.bloom;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Component
public class ListPageHandler extends BloomHandler {

    private final RedissonClient redissonClient;

    public ListPageHandler(@Qualifier("redisson") RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void handle(Object[] args) {
        Integer currentPage = (Integer) args[0];
        Integer year = (Integer) args[1];

        if (Objects.equals(year, Integer.MIN_VALUE)) {
            Boolean bit = redissonClient.getBitSet(Const.BLOOM_FILTER_PAGE).get(currentPage);
            if (Boolean.FALSE.equals(bit)) {
                throw new MissException(NO_FOUND.getMsg() + currentPage + " page");
            }
        } else {
            Boolean bit = redissonClient.getBitSet(Const.BLOOM_FILTER_YEAR_PAGE + year).get(currentPage);
            if (Boolean.FALSE.equals(bit)) {
                throw new MissException("Not found " + year + " year " + currentPage + " page");
            }
        }
    }
}