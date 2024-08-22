package org.chiu.micro.exhibit.bloom;

import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.lang.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.chiu.micro.exhibit.lang.ExceptionMessage.NO_FOUND;

@Component
@RequiredArgsConstructor
public class ListPageHandler extends BloomHandler {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void handle(Object[] args) {
        Integer currentPage = (Integer) args[0];
        Integer year = (Integer) args[1];

        if (Objects.equals(year, Integer.MIN_VALUE)) {
            Boolean bit = redisTemplate.opsForValue().getBit(Const.BLOOM_FILTER_PAGE.getInfo(), currentPage);
            if (Boolean.FALSE.equals(bit)) {
                throw new MissException(NO_FOUND.getMsg() + currentPage + " page");
            }
        } else {
            Boolean bit = redisTemplate.opsForValue().getBit(Const.BLOOM_FILTER_YEAR_PAGE.getInfo() + year, currentPage);
            if (Boolean.FALSE.equals(bit)) {
                throw new MissException("Not found " + year + " year " + currentPage + " page");
            }
        }
    }
}
