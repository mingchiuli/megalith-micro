package wiki.chiu.micro.exhibit.adapter.out.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;

import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

class RedisBlogExistenceStoreTest {

    @Test
    void lookupFailsOpenWhenRedisIsUnavailable() {
        RedissonClient redisson = mock(RedissonClient.class);
        when(redisson.getBucket(RedisBlogExistenceStore.READY_KEY))
            .thenThrow(new RedisConnectionException("unavailable"));
        RedisBlogExistenceStore store = new RedisBlogExistenceStore(redisson);

        assertEquals(BlogExistenceStore.State.UNKNOWN, store.lookup(7L));
    }
}
