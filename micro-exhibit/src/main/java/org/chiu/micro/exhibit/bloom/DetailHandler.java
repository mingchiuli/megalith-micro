package org.chiu.micro.exhibit.bloom;

import org.chiu.micro.exhibit.exception.MissException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import static org.chiu.micro.exhibit.lang.Const.BLOOM_FILTER_BLOG;
import static org.chiu.micro.exhibit.lang.ExceptionMessage.NO_FOUND;

@Component
@RequiredArgsConstructor
public class DetailHandler extends BloomHandler {

    private final RedissonClient redissonClient;

    @Override
    public void handle(Object[] args) {
        Long blogId = (Long) args[0];
        Boolean bit = redissonClient.getBitSet(BLOOM_FILTER_BLOG.getInfo()).get(blogId);
        if (Boolean.FALSE.equals(bit)) {
            throw new MissException(NO_FOUND.getMsg() + blogId + " blog");
        }
    }
}
