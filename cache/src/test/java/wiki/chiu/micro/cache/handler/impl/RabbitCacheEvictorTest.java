package wiki.chiu.micro.cache.handler.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;

class RabbitCacheEvictorTest {

    @Test
    void publisherNackMakesEvictionFailClosed() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RedissonClient redisson = mock(RedissonClient.class);
        RLock lock = mock(RLock.class);
        RKeys keys = mock(RKeys.class);
        CacheProperties properties = new CacheProperties();
        properties.getEviction().getRabbit().setExchange("cache.exchange");
        when(redisson.getLock("megalithRemoteLock:cache:v1:key")).thenReturn(lock);
        when(lock.tryLock(5000, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(redisson.getKeys()).thenReturn(keys);
        doAnswer(
            invocation -> {
                CorrelationData correlation = invocation.getArgument(3);
                correlation
                    .getFuture()
                    .complete(new CorrelationData.Confirm(false, "broker nack"));
                return null;
            })
            .when(rabbitTemplate)
            .convertAndSend(
                eq("cache.exchange"),
                eq(""),
                any(CacheEvictionMessage.class),
                any(CorrelationData.class));
        RabbitCacheEvictor evictor =
            new RabbitCacheEvictor(
                rabbitTemplate,
                redisson,
                Caffeine.newBuilder().build(),
                properties,
                new CacheMetrics(null));

        assertThatThrownBy(() -> evictor.evict(Set.of("cache:v1:key")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cache eviction broadcast was not confirmed")
            .hasRootCauseMessage("Cache eviction broadcast was nacked: broker nack");
    }
}
