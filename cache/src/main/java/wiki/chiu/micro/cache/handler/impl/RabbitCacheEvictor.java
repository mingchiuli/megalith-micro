package wiki.chiu.micro.cache.handler.impl;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import wiki.chiu.micro.cache.config.CacheProperties;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;
import wiki.chiu.micro.cache.metrics.CacheMetrics;
import wiki.chiu.micro.cache.store.LocalCacheEntry;

public final class RabbitCacheEvictor extends AbstractCacheEvictor {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final long confirmTimeoutMillis;

    public RabbitCacheEvictor(
        RabbitTemplate rabbitTemplate,
        RedissonClient redissonClient,
        Cache<@NonNull String, LocalCacheEntry> localCache,
        CacheProperties properties,
        CacheMetrics metrics) {
        super(redissonClient, localCache, properties, metrics);
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = properties.getEviction().getRabbit().getExchange();
        this.confirmTimeoutMillis =
            properties.getEviction().getRabbit().getConfirmTimeout().toMillis();
    }

    @Override
    protected String transport() {
        return "rabbit";
    }

    @Override
    protected void broadcast(CacheEvictionMessage message) {
        CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, "", message, correlation);
        try {
            CorrelationData.Confirm confirm =
                correlation.getFuture().get(confirmTimeoutMillis, TimeUnit.MILLISECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException("Cache eviction broadcast was nacked: " + confirm.reason());
            }
            if (correlation.getReturned() != null) {
                throw new IllegalStateException(
                    "Cache eviction broadcast was returned: "
                        + correlation.getReturned().getReplyText());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for cache eviction confirmation", e);
        } catch (Exception e) {
            throw new IllegalStateException("Cache eviction broadcast was not confirmed", e);
        }
    }
}
