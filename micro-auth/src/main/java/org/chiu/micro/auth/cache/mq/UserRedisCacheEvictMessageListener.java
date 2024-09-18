package org.chiu.micro.auth.cache.mq;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.chiu.micro.auth.cache.config.CacheKeyGenerator;
import org.chiu.micro.auth.config.CacheUserEvictRabbitConfig;
import org.chiu.micro.auth.constant.UserAuthMenuOperateMessage;
import org.chiu.micro.auth.lang.AuthMenuOperateEnum;
import org.chiu.micro.auth.wrapper.AuthWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


/**
 * @author mingchiuli
 * @create 2021-12-13 11:38 AM
 */
@Component
@RequiredArgsConstructor
public class UserRedisCacheEvictMessageListener {

    private final CacheKeyGenerator cacheKeyGenerator;

    private final RedissonClient redissonClient;

    private final RabbitTemplate rabbitTemplate;

    private static final String QUEUE = "user.auth.menu.change.queue.auth";

    @RabbitListener(queues = QUEUE,
                    concurrency = "10",
                    messageConverter = "jsonMessageConverter",
                    executor = "mqExecutor")
    @SneakyThrows
    public void handler(UserAuthMenuOperateMessage message, Channel channel, Message msg) {
        AuthMenuOperateEnum operateEnum = AuthMenuOperateEnum.of(message.getType());
        List<String> roles = message.getRoles();
        Set<String> keys = new HashSet<>();
        if (AuthMenuOperateEnum.MENU.equals(operateEnum) || AuthMenuOperateEnum.AUTH_AND_MENU.equals(operateEnum)) {
            Method method = AuthWrapper.class.getMethod("getCurrentUserNav", String.class);
            roles.forEach(role -> keys.add(cacheKeyGenerator.generateKey(method, role)));
        }

        if (AuthMenuOperateEnum.AUTH.equals(operateEnum) || AuthMenuOperateEnum.AUTH_AND_MENU.equals(operateEnum)) {
            Method getAuthoritiesByRoleCodeMethod = AuthWrapper.class.getMethod("getAuthoritiesByRoleCode", String.class);
            roles.forEach(role -> keys.add(cacheKeyGenerator.generateKey(getAuthoritiesByRoleCodeMethod, role)));
            Method getAllSystemAuthoritiesMethod = AuthWrapper.class.getMethod("getAllSystemAuthorities");
            keys.add(cacheKeyGenerator.generateKey(getAllSystemAuthoritiesMethod));
        }

        redissonClient.getKeys().delete(keys.toArray(new String[0]));
        rabbitTemplate.convertAndSend(CacheUserEvictRabbitConfig.CACHE_EVICT_FANOUT_EXCHANGE, "", keys);
    }
}
